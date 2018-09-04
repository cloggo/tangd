(ns sqlite.core
  (:require
   #_[promesa.core :as p]
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
   (fn [callback]
     (sqlite3/Database.
      db-name
      (fn [err]
        (if err (interop/log-error err)
            (this-as db (callback db))))))))

(defn db-close [db]
  (oops/ocall db :close (fn [err]
                          #_(println "closed db")
                          (when err (interop/log-error err)))))


(defn on-cmd [cmd stmt & params]
  (defn cmd-wrap
    [callback]
    (fn [db]
      (let [db-cmd (interop/bind db cmd)]
        #_(println stmt)
        (db-cmd stmt (into-array params)
                (fn [err]
                  (if err (interop/log-error err)
                      (this-as result (callback result)))))))))

;; cmd-wrap2 cmd-wrap1 cmd-wrap0
;; cmd-wrap0  => x0  (close database)
;; (fn [_] (x0 db)) => w0
;; (cmd-wrap1 w0) => x1
;; (fn [_] (x1 db)) => w1
;; (cmd-wrap2 w1) => x2
;; (fn [_] (x2 db)) => w2

(defn serialize-wrapper [handler]
  (fn ([v0] (fn [_] (handler v0)))
    ([v0 v1] (fn [_] (handler (v1 v0))))))

;; create execution stack (chaining callback)
(defn serializer [wrapper-func]
  (defn serialize
    ([cmd-wrap-vec]
         (serialize (wrapper-func (peek cmd-wrap-vec)) cmd-wrap-vec))
    ([executor cmd-wrap-vec]
     (let [cmd-wrap-vec (pop cmd-wrap-vec)
           cmd-wrap (peek cmd-wrap-vec)]
       #_(println cmd-wrap)
       (if (empty? cmd-wrap-vec)
         executor
         (recur (wrapper-func executor cmd-wrap) cmd-wrap-vec)))))
  serialize)



;; Initializtion
;; ===============

(defn init-db [db-tables db-indexes]
  (let [db-h (on-db)
        table-creators (mapv #(on-cmd :run %) db-tables)
        index-creators (mapv #(on-cmd :run %) db-indexes)
        cmd-wrap-vec (conj (into table-creators index-creators) db-close)
        executor ((serializer (serialize-wrapper db-h)) cmd-wrap-vec)]
    #_(println cmd-wrap-vec)
    (executor)))
