(ns app.service.keys
  (:require
   [clojure.string :as s]
   [clojure.core.async :as async]
   [async.sqlite.core :as sqlite]
   [jose.core :as jose]
   [app.service.schema :as schema]))

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
    (jose/jws-sig payload sig jwks)))


(defn rotate-keys []
  (let [jwk-es512 (jose/jwk-gen "ES512")
        jwk-ecmr (jose/jwk-gen "ECMR")
        payload (create-payload jwk-es512 jwk-ecmr)
        jws (create-jws payload jwk-es512)]
    [jwk-es512 jwk-ecmr payload jws]))


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
      #_(println thp-id-vec)
      (async/map (insert-thp-jwk db jwk-id) thp-id-vec))))


(defn reset-jws-table [db]
  (sqlite/on-cmd db :run
                 (s/join [schema/drop-jws-table
                          schema/create-jws-table
                          schema/create-jws-jwk-index])))


(defn select-all-jwk [db]
  (sqlite/on-cmd db :each schema/select-all-jwk))


(defn insert-jws [db payload default-es512]
  (fn [result]
    (let [jwk (jose/json-loads (.-jwk result))]
      (when (jose/jwk-prm jwk true "sign")
        (let [jwk-id (.-jwk_id result)
              jws (create-jws payload jwk default-es512)]
          (println (jose/json-dumps jws))
          (sqlite/on-cmd db :run jwk-id (jose/json-dumps jws)))))))

