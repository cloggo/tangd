(ns app.service.rec
  (:require
   [app.service.schema :as schema]
   [async-sqlite.core :as sqlite]))

(defn get-jwk-from-thp [db thp]
  (sqlite/on-cmd db :get schema/get-jwk-from-thp thp))

