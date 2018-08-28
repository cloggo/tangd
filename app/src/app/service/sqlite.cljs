(ns app.service.sqlite
  (:require
   [oops.core :as oops]
   [jose.jose :as jose]
   [sqlite3]))

;; SCHEMA
;; =======
;;
;; TABLE: jwk
;; (jwk_id, jwk)
;;
;; TABLE: thumbprint
;; (thumbprint, jwk_id)
;;
;; 1. generate ECMR
;; 2. generate ES512
;; 3. calculate the jws from 1 and 2

(def jwk-es512 (jose/jwk-gen "ES512"))
(def jwk-ecmr (jose/jwk-gen "ECMR"))

(println "get-alg: " (jose/get-alg (jose/get-alg-kind :JOSE_HOOK_ALG_KIND_HASH)))

(defn rotate-keys [dbname]
  (let [db (sqlite3/Database. dbname)]
    (oops/ocall db :close)))

