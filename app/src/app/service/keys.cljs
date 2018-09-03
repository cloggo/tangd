(ns app.service.keys
  (:require
   [interop.core :as interop]
   [oops.core :as oops]
   [jose.core :as jose]
   [sqlite.core :as sqlite]
   [sqlite3]) )


(def insert-jwk-stmt "INSERT INTO jwk(jwk) VALUES(?);")
(def insert-thp-stmt "INSERT INTO thp(thp) VALUES(?);")
(def insert-jws-stmt "INSERT INTO jws(jwk_id, jws) VALUES(?,?);")
(def insert-thp-jwk-stmt "INSERT INTO thp_jwk(thp_id, jwk_id) VALUES(?,?);")
(def select-list-jwk-stmt "SELECT jwk_id, jwk FROM jwk;")


(defn create-payload [jwk-es512 jwk-ecmr]
  (let [jwk-es512-pub (jose/jwk-pub jwk-es512)
        jwk-ecmr-pub (jose/jwk-pub jwk-ecmr) ]
    (jose/jwks->keys jwk-es512-pub jwk-ecmr-pub) ))

(defn create-jws [payload & jwks]
  (let [n (count jwks)
        tmp "{\"protected\":{\"cty\":\"jwk-set+json\"}}"
        sig (jose/json-array (repeat n (jose/json-loads tmp)))
        jwks (jose/json-array jwks)]
    #_(println (jose/json-dumps (jose/jws-sig payload sig jwks)))
    (jose/jws-sig payload sig jwks)))

(defn rotate-keys []
  (sqlite/on-serialize
   (fn [db]
     (let [jwk-es512 (jose/jwk-gen "ES512")
           jwk-ecmr (jose/jwk-gen "ECMR")
           payload (create-payload jwk-es512 jwk-ecmr)
           jws (create-jws payload jwk-es512)]
       identity))))
