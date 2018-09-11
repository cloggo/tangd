(ns app.controller.keys
  (:require
   [async.restify.core :as restify]
   [sqlite.core :as sqlite]
   [async.sqlite.core :as sqlite* :refer-macros [transaction]]
   [app.coop :as coop]
   [async.core :as async* :refer-macros [<?* <?_]]
   [async-error.core :refer-macros [go-try <?] :refer [throw-err]]
   [re-frame.core :as rf]
   [clojure.core.async :as async :refer [go <!]]
   [app.service.keys :as keys]))

(defn rotate-keys [db init-vals]
  (let [[es512 ecmr payload jws] init-vals]
    (sqlite*/transaction
     db [(fn [_] {:status :CREATED})
         (fn [_] {:status :INTERNAL_SERVER_ERROR
                  :error "Change not committed."})
         async*/error?]
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
                (<?) (sqlite*/handle-db-result)))))


(def handler (restify/handle-route restify-route-event))
