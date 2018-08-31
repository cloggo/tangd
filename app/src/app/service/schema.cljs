(ns app.service.schema
  (:require [sqlite.core :as sqlite]))

;; SCHEMA
;; =======
;;
;; TABLE: jwk
;; (jwk_id, jwk)
;;
;; TABLE: thumbprint
;; (thumbprint_id, thumbprint)

;;; TABLE: thumbprint_jwk
;; (thumbprint_id, jwk_id)

;;; TABLE: jws
;; (jwk_id, jws)

;;
;; 1. generate ECMR
;; 2. generate ES512
;; 3. calculate the jws from 1 and 2

(def jwk-table ["jwk"
                [["jwk_id" "INTEGER" "PRIMARY KEY" "DESC"]
                 ["jwk" "TEXT"]]])

(def thp-table ["thp"
                [["thp_id" "INTEGER" "PRIMARY KEY" "DESC"]
                 ["thp" "TEXT"]]])

(def jws-table ["jws"
                [["jwk_id" "INTEGER"]
                 ["jws" "TEXT"]]
                [[ "FOREIGN KEY" [ "jwk_id" ] "jwk" [ "jwk_id" ] "ON" "DELETE" "CASCADE"]]])

(def thp-jwk-table ["thp_jwk"
                    [["thp_id" "INTEGER"]
                     ["jwk_id" "INTEGER"]]
                    [[ "FOREIGN KEY" ["thp_id"] "thp" ["thp_id"] "ON" "DELETE" "CASCADE"]
                     [ "FOREIGN KEY" [ "jwk_id" ] "jwk" [ "jwk_id" ] "ON" "DELETE" "CASCADE"] ]])

(def thp-thp-index ["UNIQUE" "thp_thp_index" "thp" [["thp"]]])
(def jws-jwk-index ["UNIQUE" "jws_jwk_index" "jws" [[ "jwk_id" ]]])
(def thp-jwk-thp-jwk-index [nil "thp_jwk_thp_jwk_index" "thp_jwk" [[ "thp_id" ] [ "jwk_id" ]]])


(println (sqlite/create-index-stmt thp-jwk-thp-jwk-index))
(println (sqlite/create-table-stmt thp-jwk-table))
