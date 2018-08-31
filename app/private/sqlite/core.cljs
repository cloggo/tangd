(ns sqlite.core
  (:require
   [clojure.string :as string]
   [oops.core :as oops]
   [jose.jose :as jose]
   [sqlite3]))

(def ^{:dynamic true :private true} *db-name* ":memory:")

(defn set-db-name! [name] (set! *db-name* name))

(defn on-db* [dbname call-back]
  (let [db (sqlite3/Database. dbname)]
    (call-back db)
    (oops/ocall db :close)))

(defn on-db [call-back]
  (on-db* *db-name* call-back))

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
                              (string/join "," (mapv #(string/join " " %) columns))
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
                      (string/join ", " [ columns constraints ])
                      ");"])))
