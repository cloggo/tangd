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
  [(fn [success] (rf/dispatch [:http-response {:payload {:msg "ok"}} ->context]))
   (fn [err] (rf/dispatch [:http-response {:status :METHOD_FAILURE :error err} ->context]))])


(defn insert-jwk [db jwk]
  (sqlite/on-cmd db :run schema/insert-jwk (jose/json-dumps jwk)))


(defn insert-thp-jwk [db jwk-id]
  (fn [result]
    (let [thp-id (.-lastID result)]
      (sqlite/on-cmd db :run schema/insert-thp-jwk thp-id jwk-id))))


(defn insert-thp [db jwk]
  (fn [result]
    (let [jwk-id (.-lastID result)
          algs (jose/get-alg (jose/get-alg-kind :JOSE_HOOK_ALG_KIND_HASH))
          thp-vec (mapv #(jose/calc-thumbprint jwk %) algs)
          thp-id-vec (mapv #(sqlite/on-cmd db :run schema/insert-thp %) thp-vec)]
      (sqlite/map* (insert-thp-jwk db jwk-id) thp-id-vec))))


(defn rotate-keys [db ->context]
  (let [[es512 ecmr payload jws] (keys/rotate-keys)]
    (->> (insert-jwk db ecmr)
        ((insert-thp db ecmr))
        ((apply sqlite/go (restify-handlers ->context))))))


(coop/restify-route-event
 :rotate-keys
 (fn [{:keys [db]} [->context]]
   (rotate-keys (get-in db [:sqlite :db]) ->context)
   {}))

