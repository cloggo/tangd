(ns app.service.sqlite
  (:require
   [oops.core :as oops]
   [sqlite3]))

(defn rotate-keys [dbname]
  (let [db (sqlite3/Database. dbname)]
    (oops/ocall db :close)))

