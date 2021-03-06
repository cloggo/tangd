(ns app.service.keys
  (:require
   [clojure.string :as str]
   [cljs-async.core :as async]
   [async-sqlite.core :as sqlite]
   [jose.core :as jose]
   [oops.core :as oops]
   [app.service.schema :as schema]))

(def jose-hash-algs (jose/get-alg (jose/get-alg-kind :JOSE_HOOK_ALG_KIND_HASH)))

(def ^:dynamic *default-jws* nil)

(def ^:dynamic *ip-whitelist* ["::1" "127.0.0.1"])

(defn default-jws []
  *default-jws*)

(defn ip-whitelist [] *ip-whitelist*)

(defn update-ip-whitelist
  ([] (update-ip-whitelist (oops/oget js/process :env "IP_WHITELIST")))
  ([ips]
   (set! *ip-whitelist*
         (into *ip-whitelist*
               (str/split ips  #" ")))))


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
    (let [thp-id (oops/oget result :lastID)]
      (sqlite/on-cmd db :run schema/insert-thp-jwk thp-id jwk-id))))


(defn insert-thp [db jwk]
  (fn [result]
    (let [jwk-id (oops/oget result :lastID)
          algs (jose/get-alg (jose/get-alg-kind :JOSE_HOOK_ALG_KIND_HASH))
          thp-vec (mapv #(jose/calc-thumbprint jwk %) algs)
          thp-id-vec (mapv #(sqlite/on-cmd db :run schema/insert-thp %) thp-vec)]
      thp-id-vec)))

(defn drop-jws-table [db]
  (sqlite/on-cmd db :run schema/drop-jws-table))

(defn create-jws-table [db]
  (sqlite/on-cmd db :run schema/create-jws-table))

(defn create-jws-jwk-index [db]
  (sqlite/on-cmd db :run schema/create-jws-jwk-index))

(defn select-all-jwk [db]
  ((sqlite/on-cmd* 16) db :each schema/select-all-jwk))


(defn insert-jws [db payload default-es512]
  (fn [result]
    (let [jwk (jose/json-loads (oops/oget result :jwk))]
      (when (jose/jwk-prm jwk true "sign")
        (let [jwk-id (oops/oget result :jwk_id)
              jws (create-jws payload jwk default-es512)]
          (sqlite/on-cmd db :run schema/insert-jws jwk-id (jose/json-dumps jws)))))))


(defn cache-defaults [jws]
  (set! *default-jws* (jose/json-dumps jws)))


(defn init-db [db [stmt0 & stmts]]
  (reduce (fn [ch stmt]
            (async/go-try
             (async/<? ch)
             (async/<? (sqlite/on-cmd db :run stmt))))
          (sqlite/on-cmd db :run stmt0)
          stmts))
