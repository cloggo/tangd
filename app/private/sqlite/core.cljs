(ns sqlite.core
  (:require
   [promesa.core :as p]
   [interop.core :as interop]
   [clojure.string :as string]
   [oops.core :as oops]
   #_[func.core :as func]
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

(defn on-cmd* [db cmd stmt & params]
  (let [db-cmd (interop/bind db cmd)]
    (fn cmd-wrap
      ([] (db-cmd stmt (into-array params)
               (fn [err] (when err (log-error-handler err)))))
      ([callback]
       (cmd-wrap callback log-error-handler))
      ([callback err-handler]
       (db-cmd stmt (into-array params) ((cmd handler-sig) callback err-handler))))))


(defn on-cmd [db cmd stmt & params]
  (p/promise
   (fn [resolve reject]
     ((apply on-cmd* db cmd stmt params)
      (fn [result] (resolve result))
      (fn [err] (reject err))))))


(defn on-cmd-stmt
  ([stmt cmd]
   (let [stmt-cmd (interop/bind stmt cmd)]
     (p/promise
      (fn [resolve reject]
        (stmt-cmd (fn [err]
                    (if err (reject err)
                        (this-as result (resolve result)))))))))

  ([stmt cmd params]
   (let [stmt-cmd (interop/bind stmt cmd)]
     (p/promise
      (fn [resolve reject]
        (stmt-cmd (into-array params)
                  (fn [err]
                    (if err (reject err)
                        (this-as result (resolve result))))))))))

(defn stmt-finalize [stmt]
  (oops/ocall stmt :finalize))

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
        cmds (mapv #(on-cmd* db :run %) init-stmts)
        close-f (partial db-close db)
        executor (reduce serialize-wrapper (serialize-wrapper close-f) cmds)
        #_executor #_(func/foldr serialize-wrapper (serialize-wrapper close-f) cmds)]
    #_(println cmd-wrap-vec)
    (executor)))
