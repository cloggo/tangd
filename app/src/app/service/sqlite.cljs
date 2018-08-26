(ns app.service.sqlite
  (:require
   [oops.core :as oops]
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
(defn rotate-keys [dbname]
  (let [db (sqlite3/Database. dbname)]
    (oops/ocall db :close)))

