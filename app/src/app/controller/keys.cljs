(ns app.controller.keys
  (:require
   [async.restify.core :as restify]
   [sqlite.core :as sqlite]
   [async.sqlite.core :as sqlite* :refer-macros [transaction]]
   [async.core :as async :refer-macros [<?* <?_ <? go-try <!]]
   [app.service.keys :as keys]))

(defn rotate-keys [db init-vals]
  (let [[es512 ecmr payload jws] init-vals]
    (sqlite*/transaction
     db [(fn [_] {:status :CREATED})]
     (go-try
      (-> (keys/insert-jwk db ecmr)
          (<?) ((keys/insert-thp db ecmr))
          (<?_ (keys/insert-jwk db es512))
          (<?) ((keys/insert-thp db es512))
          (<?_ (keys/drop-jws-table db))
          (<?_ (keys/create-jws-table db))
          (<?_ (keys/create-jws-jwk-index db))
          (<?_ (keys/select-all-jwk db))
          (<?* number? (keys/insert-jws db payload es512)))))))


(defn restify-route-event [ch]
  (let [init-vals (keys/rotate-keys)
        [es512 ecmr payload jws] init-vals
        sqlite-db (sqlite/on-db)]
    (go-try (-> (<?_ ch (rotate-keys sqlite-db init-vals))
                (<!) (sqlite*/handle-db-result)))))


(def handler (restify/handle-route restify-route-event))
