(ns app.controller.keys
  (:require
   [async.restify.core :as restify]
   [sqlite.core :as sqlite]
   [async.sqlite.core :as sqlite*]
   [app.coop :as coop]
   [async.core :as async* :refer-macros [<?* <?_]]
   [async-error.core :refer-macros [go-try <?] :refer [throw-err]]
   [re-frame.core :as rf]
   [clojure.core.async :as async :refer [go <!]]
   [app.service.keys :as keys]))


(defn rotate-keys [db init-vals]
  (let [[es512 ecmr payload jws] init-vals]
    (go-try
     (-> (sqlite*/begin-transaction db)
         (<?_ (keys/insert-jwk db ecmr))
         (<?) ((keys/insert-thp db ecmr))
         (<?_ (keys/insert-jwk db es512))
         (<?) ((keys/insert-thp db es512))
         (<?_ (keys/drop-jws-table db))
         (<?_ (keys/create-jws-table db))
         (<?_ (keys/create-jws-jwk-index db))
         (<?_ (keys/select-all-jwk db))
         (<?* number? (keys/insert-jws db payload es512))
         (<?) (#(if %
                  (do (sqlite*/commit-transaction db) {:status :CREATED})
                  (do (sqlite*/rollback-transaction db) {:status :INTERNAL_SERVER_ERROR
                                                      :error "Change not committed."})))))))


(defn handle-db-result [result]
  (if (instance? js/Error result)
    {:status :INTERNAL_SERVER_ERROR :error result}
    result))


(defn restify-route-event [ch]
  (let [init-vals (keys/rotate-keys)
        [es512 ecmr payload jws] init-vals
        sqlite-db (sqlite/on-db)]
    (go-try (-> (<?_ ch (rotate-keys sqlite-db init-vals))
                (<?) (handle-db-result)))))


(def handler (restify/handle-route restify-route-event))
