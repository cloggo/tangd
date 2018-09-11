(ns app.controller.keys
  (:require
   [app.coop :as coop]
   [async-error.core :refer-macros [go-try <?] :refer [throw-err]]
   [re-frame.core :as rf]
   [clojure.core.async :as async :refer [go take! <!]]
   [app.service.keys :as keys]))


(defn rotate-keys [db ->context]
  (let [[es512 ecmr payload jws] (get-in ->context [:jose :init-vals])]
    #_(println "rotating keys")
    (go-try
     (-> (keys/begin-transaction db)
         (<?) ((fn [_](keys/insert-jwk db ecmr)))
         (<?) ((keys/insert-thp db ecmr))
         (<?) ((fn [_] (keys/insert-jwk db es512)))
         (<?) ((keys/insert-thp db es512))
         (<?) ((fn [_] (keys/drop-jws-table db)))
         (<?) ((fn [_] (keys/create-jws-table db)))
         (<?) ((fn [_] (keys/create-jws-jwk-index db)))
         (<?) ((fn [_] (keys/select-all-jwk db)))
         (#(let [insert-func (keys/insert-jws db payload es512)]
             (go-try
              (loop [doc (<? %)]
                (if (number? doc) doc
                    (do (insert-func doc)
                        (recur (<? %))))))))
         (<?) (#(if %
                  (do (keys/commit-transaction db) {:status :CREATED})
                  (do (keys/rollback-transaction db) {:status :INTERNAL_SERVER_ERROR
                                                      :error "Change not committed."})))))))


(coop/restify-route-event
 :rotate-keys
 (fn [{:keys [db]} [->context]]
   (let [init-vals (keys/rotate-keys)
         ->context (assoc-in ->context [:jose :init-vals] init-vals)
         [es512 ecmr payload jws] init-vals
         sqlite-db (get-in db [:sqlite :db])]
     (go (-> (rotate-keys sqlite-db ->context)
             (<!) (#(if (instance? js/Error %)
                      {:status :INTERNAL_SERVER_ERROR :error %} %))
             (#(rf/dispatch [:http-response % ->context]))))
     {:db (assoc-in db [:jose] {:default-jws jws :payload payload})})))
