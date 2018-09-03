(ns sqlite.core
  (:require
   [promesa.core :as p]
   [interop.core :as interop]
   [clojure.string :as string]
   [oops.core :as oops]
   [sqlite3]))

(def ^{:dynamic true :private true} *db-name* ":memory:")

;; INDEX
;; =====

(defn create-index-stmt [index-spec]
  (let [[unique? index-name table-name columns] index-spec]
    (string/join " " (remove string/blank?
                             ["CREATE"
                              unique?
                              "INDEX" "IF NOT EXISTS"
                              index-name
                              "ON"
                              table-name
                              "("
                              (string/join ", " (mapv #(string/join " " %) columns))
                              ");"]))))




;; TABLE
;;; ======

(defn create-foreign-key [columns foreign-table-name foreign-columns & clause]
  (let [clause (string/join " " clause)
        foreign-columns (string/join ["(" (string/join "," foreign-columns) ")"])
        columns (string/join ["(" (string/join "," columns) ")"])]
    (string/join " " ["FOREIGN KEY" columns
                      "REFERENCES" foreign-table-name foreign-columns clause])))

(def table-constraints-handlers {"FOREIGN KEY" create-foreign-key})


(defn create-columns [columns]
  (string/join ", " (mapv #(string/join " " %) columns)))


(defn create-constraint [constraint]
  (let [[constraint-name & constraint-spec] constraint]
    (apply (get table-constraints-handlers constraint-name) constraint-spec)))


(defn create-constraints [table-constraints]
  (string/join ", " (mapv create-constraint table-constraints)))


(defn create-table-stmt [table-spec]
  (let [[table-name columns table-constraints] table-spec
        columns (create-columns columns)
        constraints (create-constraints table-constraints)]
    (string/join " " ["CREATE TABLE"
                      "IF NOT EXISTS"
                      table-name
                      "("
                      (string/join ", " (remove string/blank? [ columns constraints ]))
                      ");"])))

;; DB  commands
;; ============

(defn sqlite-verbose[] (oops/ocall sqlite3 :verbose))

(defn set-db-name! [name] (set! *db-name* name))


(defn on-db
  ([call-back] (on-db *db-name* call-back))
  ([dbname call-back]
      (sqlite3/Database.
       dbname
       (fn [err]
         (this-as db
           (if err
             (interop/log-error err)
             (p/then
              (call-back db)
              (fn [_] (db-close db)))))))))

(defn db-close [db]
  (oops/ocall db :close (fn [err] (when err (interop/log-error err)))))

(defn run-cmd-handler [resolve reject]
  (fn [err]
    (this-as result (if err (reject err) (resolve result)))))

(def handlers
  {:run run-cmd-handler})

(defn on-cmd [db cmd stmt & params]
  (p/promise
   (fn [resolve reject]
     (let [db-cmd (interop/bind db cmd)]
       (db-cmd stmt (into-array params) ((cmd handlers) resolve reject))))))

;; Initialization
;; ===============

(defn create-schema [db stmt-creator schema-vec]
  (mapv #(on-cmd db :run (stmt-creator %)) schema-vec))

(defn init-db [db-tables db-indexes]
  (on-db
   (fn [db]
     (-> (p/all (create-schema db create-table-stmt db-tables))
         (p/then (fn [_] (p/all (create-schema db create-index-stmt db-indexes))))))))
