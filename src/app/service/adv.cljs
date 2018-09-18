(ns app.service.adv
  (:require
   [app.service.schema :as schema]
   [async.sqlite.core :as sqlite]))

(defn get-jws-from-thp [db thp]
  (sqlite/on-cmd db :get schema/get-jws-from-thp thp))

