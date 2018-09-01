(ns app.service.keys
  (:require
   [interop.core :as interop]
   [oops.core :as oops]
   [jose.core :as jose]
   [sqlite.core :as sqlite]
   [sqlite3]) )


(def jwk-es512 (jose/jwk-gen "ES512"))
(def jwk-ecmr (jose/jwk-gen "ECMR"))


(def insert-jwk-stmt "INSERT INTO jwk(jwk) VALUES(?);")


(defn insert-jwk [jwk]
  (sqlite/on-db-cmd
   :run insert-jwk-stmt
   #js [jwk]
   (sqlite/cmd-result-handler #(interop/log (.-lastID %)))))


(defn rotate-keys []
  (insert-jwk jwk-ecmr))

;; (println "get-alg: " (jose/get-alg (jose/get-alg-kind :JOSE_HOOK_ALG_KIND_HASH)))
;; (println "es512-prm: " (every? #(jose/jwk-prm jwk-es512 true %) ["sign" "verify"]))
;; (println "ecmr-prm: " (jose/jwk-prm jwk-ecmr true "deriveKey"))

(def jwk-es512-thp (jose/calc-thumbprint jwk-es512))
(def jwk-ecmr-thp (jose/calc-thumbprint jwk-ecmr))

(def jwk-es512-pub (jose/jwk-pub jwk-es512))
(def jwk-ecmr-pub (jose/jwk-pub jwk-ecmr))

(def payload (jose/jwks->keys jwk-es512-pub jwk-ecmr-pub))

;; (println "thp: " jwk-es512-thp jwk-ecmr-thp)

(def tmp "{\"protected\":{\"cty\":\"jwk-set+json\"}}")
(def sig (jose/json-loads tmp))


(def jws (jose/jws-sig payload sig jwk-es512))
;; (println "jws: " (jose/json-dumps jws))

(def sig-arr (jose/json-array (repeat 2 (jose/json-loads tmp))))
(def jwk-arr (jose/json-array (repeat 2 jwk-es512)))

;; (println "arr: " (jose/json-dumps sig-arr))
(def jws2 (jose/jws-sig payload sig-arr jwk-arr))
;; (println "jws2: " (jose/json-dumps jws2))

