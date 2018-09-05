(ns app.service.keys
  (:require
   [promesa.core :as p]
   [interop.core :as interop]
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


(defn clear-jws-table [db]
  (sqlite/on-cmd db :run "DELETE FROM jws; VACUUM;"))

(defn insert-jws [row]
  (print (.-jwk_id row)))

(defn insert-jwk [db jwk]
  (sqlite/on-cmd db :run schema/insert-jwk jwk))

(defn prepare-insert-thp [db]
  (sqlite/on-cmd db :prepare schema/insert-thp))

(defn insert-thp* [stmt jwk]
  (let [thp-vec (mapv #(jose/calc-thumbprint jwk %) jose-hash-algs)]
    #_(println thp-vec)
    (mapv (fn [thp] (-> (sqlite/on-cmd-stmt stmt :run [thp])
                        (p/then (fn [r] (.-lastID r)))))
          thp-vec)))

(defn insert-thp [stmt jwk]
  (-> (p/all (insert-thp* stmt jwk))
      (p/then (fn [result]
                (sqlite/stmt-finalize stmt)
                result))))

(defn cache-jws [db]
  (sqlite/on-cmd db :each schema/select-all-jwk))

(defn rotate-keys []
  (let [jwk-es512 (jose/jwk-gen "ES512")
        jwk-ecmr (jose/jwk-gen "ECMR")
        payload (create-payload jwk-es512 jwk-ecmr)
        jws (create-jws payload jwk-es512)
        db (sqlite/on-db)]
    (-> (prepare-insert-thp db)
        (p/then insert-thp jwk-ecmr))
    (jose/json-dumps jws)))
