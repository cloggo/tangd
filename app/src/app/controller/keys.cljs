(ns app.controller.keys
  (:require
   [app.coop :as coop]
   [async.core :as async* :refer-macros [async-stream]]
   [async-error.core :refer-macros [go-try <?] :refer [throw-err]]
   [re-frame.core :as rf]
   [clojure.core.async :as async :refer [go take! <!]]
   [app.service.keys :as keys]))


(defn rotate-keys [db ->context]
  (let [[es512 ecmr payload jws] (get-in ->context [:jose :init-vals])]
    (go-try
     (-> (keys/begin-transaction db)
         (<?) ((fn [_] (keys/insert-jwk db ecmr)))
         (<?) ((keys/insert-thp db ecmr))
         (<?) ((fn [_] (keys/insert-jwk db es512)))
         (<?) ((keys/insert-thp db es512))
         (<?) ((fn [_] (keys/drop-jws-table db)))
         (<?) ((fn [_] (keys/create-jws-table db)))
         (<?) ((fn [_] (keys/create-jws-jwk-index db)))
         (<?) ((fn [_] (keys/select-all-jwk db)))
         (async*/async-stream number? (keys/insert-jws db payload es512))
         (<?) (#(if %
                  (do (keys/commit-transaction db) {:status :CREATED})
                  (do (keys/rollback-transaction db) {:status :INTERNAL_SERVER_ERROR
                                                      :error "Change not committed."})))))))


(defn handle-result [result ->context]
  (-> (if (instance? js/Error result)
        {:status :INTERNAL_SERVER_ERROR :error result}
        result)
      (#(rf/dispatch [:http-response % ->context]))))


(coop/restify-route-event
 :rotate-keys
 (fn [{:keys [db]} [->context]]
   (let [init-vals (keys/rotate-keys)
         ->context (assoc-in ->context [:jose :init-vals] init-vals)
         [es512 ecmr payload jws] init-vals
         sqlite-db (get-in db [:sqlite :db])]
     (go (-> (rotate-keys sqlite-db ->context)
             (<!) (handle-result ->context)))
     {:db (assoc-in db [:jose] {:default-jws jws :payload payload})})))
