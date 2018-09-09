(ns app.controller.keys
  (:require
   [re-frame.core :as rf]
   [clojure.core.async :as async]
   [async.sqlite.core :as sqlite]
   [clojure.string :as s]
   [jose.core :as jose]
   [app.service.schema :as schema]
   [app.service.keys :as keys]
   [app.coop :as coop]))


(defn restify-handlers [->context]
  [(fn [sucess] (rf/dispatch [:http-response {:payload {:msg "ok"}} ->context]))
   (fn [err] (rf/dispatch [:http-response {:status :METHOD_FAILURE :error err} ->context]))])


(defn rotate-keys [db ->context]
  (let [[es512 ecmr payload jws] (keys/rotate-keys)]
    (-> (sqlite/on-cmd db :run schema/insert-jwk (jose/json-dumps ecmr))
        ((apply sqlite/then (restify-handlers ->context))))))

(coop/restify-route-event
 :rotate-keys
 (fn [{:keys [db]} [->context]]
   (rotate-keys (get-in db [:sqlite :db]) ->context)
   {}))

