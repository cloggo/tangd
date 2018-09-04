(ns sqlite.core
  (:require
   #_[promesa.core :as p]
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


(defn exec-on-cmd [db cmd stmt callback params]
  (let [db-cmd (interop/bind db cmd)]
    #_(println stmt)
    (db-cmd stmt (into-array params)
            (fn [err]
              (if err (interop/log-error err)
                  (this-as result (callback result)))))))


(defn on-cmd [db cmd stmt & params]
  (fn cmd-wrap
    [callback]
      (exec-on-cmd db cmd stmt callback params)))

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
        executor (reduce serialize-wrapper (serialize-wrapper close-f) cmds)
        #_executor #_((func/foldr serialize-wrapper) cmd-wrap-vec)]
    #_(println cmd-wrap-vec)
    (executor)))
