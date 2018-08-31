(ns app.service.keys
  (:require
   [oops.core :as oops]
   [jose.core :as jose]
   [sqlite3]) )


(defn rotate-keys [dbname]
  (let [db (sqlite3/Database. dbname)]
    (oops/ocall db :close)))


(def jwk-es512 (jose/jwk-gen "ES512"))
(def jwk-ecmr (jose/jwk-gen "ECMR"))

;; (println "get-alg: " (jose/get-alg (jose/get-alg-kind :JOSE_HOOK_ALG_KIND_HASH)))
(println "es512-prm: " (every? #(jose/jwk-prm jwk-es512 true %) ["sign" "verify"]))
(println "ecmr-prm: " (jose/jwk-prm jwk-ecmr true "deriveKey"))

(def jwk-es512-thp (jose/calc-thumbprint jwk-es512))
(def jwk-ecmr-thp (jose/calc-thumbprint jwk-ecmr))

(def jwk-es512-pub (jose/jwk-pub jwk-es512))
(def jwk-ecmr-pub (jose/jwk-pub jwk-ecmr))

(def payload (jose/jwks->keys jwk-es512-pub jwk-ecmr-pub))

(println "thp: " jwk-es512-thp jwk-ecmr-thp)

(def tmp "{\"protected\":{\"cty\":\"jwk-set+json\"}}")
(def sig (jose/json-loads tmp))


(def jws (jose/jws-sig payload sig jwk-es512))
(println "jws: " (jose/json-dumps jws))

(def sig-arr (jose/json-array (repeat 2 (jose/json-loads tmp))))
(def jwk-arr (jose/json-array (repeat 2 jwk-es512)))

;; (println "arr: " (jose/json-dumps sig-arr))
(def jws2 (jose/jws-sig payload sig-arr jwk-arr))
(println "jws2: " (jose/json-dumps jws2))

