(ns app.service.keys
  (:require
   [interop.core :as interop]
   [app.coop :as coop]
   [oops.core :as oops]
   [jose.core :as jose]
   [app.service.schema :as schema]
   [sqlite.core :as sqlite]
   [sqlite3]) )

(def jose-hash-algs (jose/get-alg (jose/get-alg-kind :JOSE_HOOK_ALG_KIND_HASH)))

(defn create-payload [jwk-es512 jwk-ecmr]
  (let [jwk-es512-pub (jose/jwk-pub jwk-es512)
        jwk-ecmr-pub (jose/jwk-pub jwk-ecmr) ]
    (jose/jwks->keys jwk-es512-pub jwk-ecmr-pub)))


(defn create-jws [payload & jwks]
  (let [n (count jwks)
        tmp "{\"protected\":{\"cty\":\"jwk-set+json\"}}"
        sig (jose/json-array (repeat n (jose/json-loads tmp)))
        jwks (jose/json-array jwks)]
    #_(println (jose/json-dumps (jose/jws-sig payload sig jwks)))
    (jose/jws-sig payload sig jwks)))


(defn rotate-keys [cfx [_ params]]
  (let [jwk-es512 (jose/jwk-gen "ES512")
        jwk-ecmr (jose/jwk-gen "ECMR")
        payload (create-payload jwk-es512 jwk-ecmr)
        jws (create-jws payload jwk-es512)]
    {:dispatch [:insert-jwk-ecmr [{:params [jwk-ecmr]}
                                  ^{:->context true} {:restify params}]]}))


(coop/reg-event-fx
 :insert-jwk-ecmr
 (fn [cfx [_ [spec]]]
   {:restify [{:payload {:message "hello"} }]}))
