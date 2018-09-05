(ns sqlite.core
  (:require
   [interop.core :as interop]
   [clojure.string :as string]
   [oops.core :as oops]
   [sqlite3]))

(def ^{:dynamic true :private true} *db-name* ":memory:")

;; DB  commands
;; ============

(defn sqlite-verbose[] (oops/ocall sqlite3 :verbose))

(defn set-db-name! [name] (set! *db-name* name))

(defn on-db
  ([] (on-db *db-name*))
  ([db-name]
   (sqlite3/Database.
    db-name
    (fn [err]
      (when err (interop/log-error err))))))


(defn db-close [db]
  (oops/ocall db :close (fn [err]
                          #_(println "closed db")
                          (when err (interop/log-error err)))))

(defn log-error-handler [err]
  (interop/log-error err))

(defn run-handler-sig
  ([callback err-handler]
   (fn [err]
     (if err (err-handler err)
         (this-as result (callback result))))))

(defn each-handler-sig
  ([callback err-handler]
   (fn [err row]
     (if err (err-handler err)
         (callback row)))))

(def handler-sig
  {:run run-handler-sig
   :each each-handler-sig
   :prepare run-handler-sig})

(defn on-cmd [db cmd stmt & params]
  (let [db-cmd (interop/bind db cmd)]
    (fn cmd-wrap
      ([] (db-cmd stmt (into-array params)
               (fn [err] (when err (log-error-handler err)))))
      ([callback]
       (cmd-wrap callback log-error-handler))
      ([callback err-handler]
       (db-cmd stmt (into-array params) ((cmd handler-sig) callback err-handler))))))

(defn on-cmd-stmt [o-stmt cmd & params]
   (let [stmt-cmd (interop/bind o-stmt cmd)
         stmt-cmd (when-not (empty? params) (apply partial stmt-cmd stmt-cmd (into-array params)))]
     (defn on-cmd-stmt*
       ([] (on-cmd-stmt* identity log-error-handler))
       ([callback] (on-cmd-stmt* callback log-error-handler))
       ([callback err-handler] (stmt-cmd ((cmd handler-sig) callback err-handler))))))

(defn stmt-finalize [stmt]
  (oops/ocall stmt :finalize))


(defn sqlite-cmd-fx [spec]
  (let [{:keys [cmd stmt params callback err-handler ->context]} spec
        ->context (update-in ->context [:db] #(or % (on-db)))
        db (:db ->context)
        callback (when callback (fn [result] (callback {:result result :->context ->context})))
        err-handler (and callback err-handler)
        handlers [callback err-handler]
        handlers (remove nil? handlers)]
    (apply (apply on-cmd db stmt params) handlers)))

;; cmd-wrap2 cmd-wrap1 cmd-wrap0
;; cmd-wrap0  => x0  (close database)
;; (fn [_] (x0 db)) => w0
;; (cmd-wrap1 w0) => x1
;; (fn [_] (x1 db)) => w1
;; (cmd-wrap2 w1) => x2
;; (fn [_] (x2 db)) => w2

(defn serialize-wrapper
  ([v0] (fn [_] (v0)))
  ([v0 v1] (fn [_] (v1 v0))))


;; Initializtion
;; ===============

(defn init-db [init-stmts]
  (let [db (on-db)
        cmds (mapv #(on-cmd db :run %) init-stmts)
        close-f (partial db-close db)
        executor (reduce serialize-wrapper (serialize-wrapper close-f) cmds)]
    (executor)))
