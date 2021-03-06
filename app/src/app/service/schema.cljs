(ns app.service.schema)

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

(def create-jwk-table
  "CREATE TABLE IF NOT EXISTS jwk
  (jwk_id INTEGER PRIMARY KEY, jwk TEXT);")

(def create-thp-table
  "CREATE TABLE IF NOT EXISTS thp
  (thp_id INTEGER PRIMARY KEY, thp TEXT);")

(def create-jws-table
  "CREATE TABLE IF NOT EXISTS jws
  (jwk_id INTEGER REFERENCES jwk (jwk_id) ON DELETE CASCADE,
  jws TEXT);")

(def create-thp-jwk-table
  "CREATE TABLE IF NOT EXISTS thp_jwk
  (thp_id INTEGER REFERENCES thp (thp_id) ON DELETE CASCADE,
   jwk_id INTEGER REFERENCES jwk (jwk_id) ON DELETE CASCADE);")

(def create-thp-thp-index
  "CREATE UNIQUE INDEX IF NOT EXISTS thp_thp ON thp (thp);")

(def create-jws-jwk-index
  "CREATE INDEX IF NOT EXISTS jws_jwk ON jws (jwk_id);")

(def create-thp-jwk-thp-index
  "CREATE UNIQUE INDEX IF NOT EXISTS thp_jwk_thp ON thp_jwk (thp_id);")

(def create-thp-jwk-jwk-index
  "CREATE INDEX IF NOT EXISTS thp_jwk_jwk ON thp_jwk (jwk_id);")

;; modification
(def insert-jwk "INSERT INTO jwk(jwk) VALUES(?);")
(def insert-thp "INSERT INTO thp(thp) VALUES(?);")
(def insert-jws "INSERT INTO jws(jwk_id, jws) VALUES(?,?);")
(def insert-thp-jwk "INSERT INTO thp_jwk(thp_id, jwk_id) VALUES(?,?);")
(def select-all-jwk "SELECT jwk_id, jwk FROM jwk;")

(def drop-jws-table "DROP TABLE IF EXISTS jws;")
(def clear-jws "DELETE FROM jws;")
(def vacuum "VACUUM;")

;; queries
(def select-default-jwk "SELECT jwk FROM jwk ORDER BY jwk_id DESC LIMIT 2;")

(def get-jws-from-thp "SELECT jws.jws FROM jws
  INNER JOIN thp_jwk ON (jws.jwk_id = thp_jwk.jwk_id)
  INNER JOIN thp ON (thp.thp_id = thp_jwk.thp_id) WHERE thp.thp = ?;")

(def get-jwk-from-thp "SELECT jwk.jwk FROM jwk
  INNER JOIN thp_jwk ON (jwk.jwk_id = thp_jwk.jwk_id)
  INNER JOIN thp ON (thp.thp_id = thp_jwk.thp_id) WHERE thp.thp = ?;")

(def init-stmts [create-jws-jwk-index create-thp-thp-index create-thp-jwk-jwk-index
                 create-thp-jwk-thp-index create-thp-jwk-table create-jws-table
                 create-thp-table create-jwk-table])

(def init-stmts* [create-jwk-table create-thp-table create-jws-table  create-thp-jwk-table
                  create-jws-jwk-index create-thp-thp-index create-thp-jwk-jwk-index
                  create-thp-jwk-thp-index ])
