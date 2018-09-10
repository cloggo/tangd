(ns app.controller.keys
  (:require
   [app.coop :as coop]
   [async-error.core :refer-macros [go-try <? <??] :refer [throw-err]]
   [re-frame.core :as rf]
   [clojure.core.async :as async :refer [go take! <!]]
   [app.service.keys :as keys]))


(defn restify-handlers [->context]
  [(fn [success] (rf/dispatch [:http-response {:payload {:msg success}} ->context]))
   (fn [err] (if err
               (rf/dispatch [:http-response {:status :METHOD_FAILURE :error err} ->context])
               err))])

(defn rotate-keys [db ->context]
  (let [[es512 ecmr payload jws] (keys/rotate-keys)
        [success error] (restify-handlers ->context)]
    (go
      (-> (go-try
           (-> (keys/insert-jwk db ecmr)
               (<?) ((keys/insert-thp db ecmr))
               (<?) ((fn [_] (keys/insert-jwk db es512)))
               (<?) ((keys/insert-thp db es512))
               (<?) ((fn [_] (keys/reset-jws-table db)))
               (<?) ((fn [_] (keys/select-all-jwk db)))
               #_(#(while true (take! % (keys/insert-jws db payload es512))))
               (#(let [insert-func (keys/insert-jws db payload es512)]
                   (go-try (while true (insert-func (<? %))))))
               (#(go-try (if % (success #_(<? %)) (success))))))
          (<!) error))))


(coop/restify-route-event
 :rotate-keys
 (fn [{:keys [db]} [->context]]
   (rotate-keys (get-in db [:sqlite :db]) ->context)
   {}))

