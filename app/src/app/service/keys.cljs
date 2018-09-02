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
(def insert-thp-stmt "INSERT INTO thp(thp) VALUES(?);")
(def insert-jws-stmt "INSERT INTO jws(jwk_id, jws) VALUES(?,?);")
(def insert-thp-jwk-stmt "INSERT INTO thp_jwk(thp_id, jwk_id) VALUES(?,?);")


(defn insert-thp [db f-thp]
  (fn [jwk-id]
    (f-thp
     (fn [thp-id]
       (sqlite/db-cmd db :run insert-thp-jwk-stmt thp-id jwk-id)))))

(defn insert-jws [db jws]
  (fn [jwk-id]
    (sqlite/db-cmd db :run insert-jws-stmt jwk-id jws)))


(defn insert-jwk* [db jwk]
  (let [f-jwk (sqlite/db-run-stmt db insert-jwk-stmt jwk)
        algs (jose/get-alg (jose/get-alg-kind :JOSE_HOOK_ALG_KIND_HASH))
        thp-vec (map #(jose/calc-thumbprint jwk %) algs)
        f-thp-vec (map #(sqlite/db-run-stmt db insert-thp-stmt %) thp-vec)
        f-thp-vec (map #(insert-thp db %) f-thp-vec) ]
    (cons f-jwk f-thp-vec)))


(defn eval-vec [v & rest]
  (let [[f & args] v
        args (concat args rest)] (apply f args)))


(defn insert-jwk-jws
  ([db jwk] (eval-vec (insert-jwk* db jwk)))
  ([db jwk jws] (eval-vec (insert-jwk* db jwk) (insert-jws db jws))))


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
  (sqlite/on-db
   (fn [db]
     (let [payload (create-payload jwk-es512 jwk-ecmr)
           jws (create-jws payload jwk-es512) ]
       (insert-jwk-jws db jwk-ecmr)
       (insert-jwk-jws db jwk-es512 jws)))))

;; (println "get-alg: " (jose/get-alg (jose/get-alg-kind :JOSE_HOOK_ALG_KIND_HASH)))
;; (println "es512-prm: " (every? #(jose/jwk-prm jwk-es512 true %) ["sign" "verify"]))
;; (println "ecmr-prm: " (jose/jwk-prm jwk-ecmr true "deriveKey"))

;; (def jwk-es512-thp (jose/calc-thumbprint jwk-es512))
;; (def jwk-ecmr-thp (jose/calc-thumbprint jwk-ecmr))

;; (println "thp: " jwk-es512-thp jwk-ecmr-thp)

;; (def tmp "{\"protected\":{\"cty\":\"jwk-set+json\"}}")
;; (def sig (jose/json-loads tmp))


;; (def jws (jose/jws-sig payload sig jwk-es512))
;; (println "jws: " (jose/json-dumps jws))

;; (def sig-arr (jose/json-array (repeat 2 (jose/json-loads tmp))))
;; (def jwk-arr (jose/json-array (repeat 2 jwk-es512)))

;; (println "arr: " (jose/json-dumps sig-arr))
;; (def jws2 (jose/jws-sig payload sig-arr jwk-arr))
;; (println "jws2: " (jose/json-dumps jws2))

