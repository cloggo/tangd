(ns sqlite.core
  (:require
   [interop.core :as interop]
   [clojure.string :as string]
   [oops.core :as oops]
   [sqlite3]))

(def ^{:dynamic true :private true} *db-name* ":memory:")

(defn sqlite-verbose[] (oops/ocall sqlite3 :verbose))

(defn set-db-name! [name] (set! *db-name* name))

(defn err-handler [err]
  (when err
    (interop/log-error (oops/oget err :message))))

(defn on-db
  ([call-back] (on-db *db-name* call-back))
  ([dbname call-back]
   (let [db (sqlite3/Database. dbname err-handler)]
     (call-back db)
     (oops/ocall db :close err-handler))))

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

(defn on-serialize
  ([call-back] (on-db (fn [db] (on-serialize db call-back))))
  ([db call-back]
   (oops/ocall db :serialize
               (fn [] (call-back db)))))


(defn on-parallelize
  ([call-back] (on-db (fn [db] (on-parallelize db call-back))))
  ([db call-back]
   (oops/ocall db :parallelize
               (fn [] (call-back db)))))


(defn on-db-cmd [db cmd stmt & bindings]
   (let [db-cmd (interop/bind db cmd)]
     (apply db-cmd stmt bindings)))


(defn init-db [db-tables db-indexes]
  (on-serialize
   (fn [db]
     (-> db
         (on-db-cmd :run (string/join ";" (mapv create-table-stmt db-tables)))
         (on-db-cmd :run (string/join ";" (mapv create-index-stmt db-indexes)))))))
