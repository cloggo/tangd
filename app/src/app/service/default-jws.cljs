(ns app.service.default-jws
  (:require
   [app.service.keys :as keys]
   [jose.core :as jose]
   [cljs-async.core :as async]
   [app.service.schema :as schema]
   [oops.core :as oops]
   [async-sqlite.core :as sqlite]))


(defn select-default-jwk [db]
  (sqlite/on-cmd db :all schema/select-default-jwk))


(defn cache-default-jws [db]
  (async/go-try
   (let [jwks (async/<? (select-default-jwk db))]
     (when-not (empty? jwks)
       (let [jwks (mapv #(oops/oget % :jwk) jwks)
             c-jwks (mapv jose/json-loads jwks)
             payload (apply keys/create-payload c-jwks)
             es512-vec (filter #(jose/jwk-prm % true "sign") c-jwks)
             jws (keys/create-jws payload (first es512-vec))]
         (keys/cache-defaults jws))))))
