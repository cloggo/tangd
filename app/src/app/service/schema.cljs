(ns app.service.schema
  (:require
   [oops.core :as oops]
   [sqlite.core :as sqlite]))

;; SCHEMA
;; =======
;;
;; TABLE: jwk
;; (jwk_id, jwk)
;;
;; TABLE: thp
;; thumbprint table
;; (thp_id, thp)

;;; TABLE: thp_jwk
;; (thp_id, jwk_id)

;;; TABLE: jws
;; (jwk_id, jws)

;;
;; 1. generate ECMR
;; 2. generate ES512
;; 3. calculate the jws from 1 and 2

(def jwk-table ["jwk"
                [["jwk_id" "INTEGER" "PRIMARY KEY" "DESC"]
                 ["jwk" "TEXT" "UNIQUE"]]])

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

(def db-tables [jwk-table thp-table jws-table thp-jwk-table])
(def db-indexes [jws-jwk-index thp-thp-index thp-jwk-thp-jwk-index])

;; (mapv #(println (sqlite/create-table-stmt %)) db-tables)
;; (mapv #(println (sqlite/create-index-stmt %)) db-indexes)

(defn init-db []
  (sqlite/init-db db-tables db-indexes))
