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

(def ^:dynamic *db* nil)


(defn on-db*
  ([] (on-db* *db-name*))
  ([db-name]
   (sqlite3/Database.
    db-name
    (fn [err]
      (when err (interop/log-error err))))))


(defn on-db
  ([name] (or *db* (set! *db* (on-db* name))))
  ([] (on-db *db-name*)))


(defn db-close [db]
  (oops/ocall db :close (fn [err]
                          #_(println "closed db")
                          (when err (interop/log-error err)))))

(defn log-error-handler [err]
  (interop/log-error err))

(defn run-handler-sig [callback err-handler]
  [(fn [err]
     (if err (err-handler err)
         (this-as result (callback result))))])

(defn each-handler-sig [callback err-handler]
  [(fn [err row]
     (if err (err-handler err)
         (callback row)))
   (fn [err nrow] (callback nrow))])

(def handler-sig
  {:run run-handler-sig
   :each each-handler-sig
   :prepare run-handler-sig})

(defn on-cmd [db cmd stmt & params]
  (let [db-cmd (interop/bind db cmd)
        db-cmd (apply partial db-cmd stmt params) ]
    (fn cmd-wrap
      ([] (db-cmd (fn [err] (when err (log-error-handler err)))))
      ([callback]
       (cmd-wrap callback log-error-handler))
      ([callback err-handler]
       (apply db-cmd ((cmd handler-sig) callback err-handler))))))

(defn on-cmd-stmt [o-stmt cmd & params]
   (let [stmt-cmd (interop/bind o-stmt cmd)
         stmt-cmd (apply partial stmt-cmd params)]
     (defn on-cmd-stmt*
       ([] (on-cmd-stmt* identity log-error-handler))
       ([callback] (on-cmd-stmt* callback log-error-handler))
       ([callback err-handler] (stmt-cmd ((cmd handler-sig) callback err-handler))))))


(defn stmt-finalize [stmt]
  (oops/ocall stmt :finalize))


(defn cmd-fx [spec]
  (let [{:keys [db cmd stmt params callback err-handler]} spec
        err-handler (and callback err-handler)
        handlers [callback err-handler]
        handlers (remove nil? handlers)]
    (apply (apply on-cmd db cmd stmt params) handlers)))


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

(defn init-db [db init-stmts]
  (let [cmds (mapv #(on-cmd db :run %) init-stmts)
        executor (reduce serialize-wrapper identity cmds)]
    (executor)))
