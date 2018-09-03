(ns app.service.keys-
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


(defn insert-jws [db jws]
  (fn [jwk-id]
    (sqlite/db-cmd db :run insert-jws-stmt jwk-id (jose/json-dumps jws))))


(defn insert-thp* [db f-thp]
  (fn [jwk-id]
    (f-thp
     (fn [thp-id]
       #_(println thp-id jwk-id)
       (sqlite/db-cmd db :run insert-thp-jwk-stmt thp-id jwk-id)))))


(defn insert-thp [db jwk]
  (let [algs (jose/get-alg (jose/get-alg-kind :JOSE_HOOK_ALG_KIND_HASH))
        thp-vec (mapv #(jose/calc-thumbprint jwk %) algs)]
    (mapv #(sqlite/db-run-stmt db insert-thp-stmt %) thp-vec)))


(defn insert-jwk [db jwk]
  (sqlite/db-run-stmt db insert-jwk-stmt (jose/json-dumps jwk)))


(defn eval-jwk-thp [db jwk]
  (let [f-jwk (insert-jwk db jwk)
        f-thp-vec (insert-thp db jwk)]
    (apply f-jwk (mapv #(insert-thp* db %) f-thp-vec))
    f-jwk))


(defn insert-jwk-jws
  ([db jwk]
   (let [f-jwk (insert-jwk db jwk)
         f-thp-vec (insert-thp db jwk)]
     (apply f-jwk (mapv #(insert-thp* db %) f-thp-vec))))

  ([db jwk jws]
   (let [f-jwk (insert-jwk db jwk)
         f-thp-vec (insert-thp db jwk)]
     (apply f-jwk (insert-jws db jws) (mapv #(insert-thp* db %) f-thp-vec)))))


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


(defn cache-jws* [db payload default-jwk]
  (fn [err row]
    (when err (throw (js/Error. "cache-jws* error!")))
    (let [jwk (jose/json-loads (.-jwk row))]
      (when (jose/jwk-prm jwk true "sign")
        (let [jws (create-jws payload jwk default-jwk)
              jwk-id (.-jwk_id row)]
          (println "id: " jwk-id)
          (sqlite/db-cmd db :run insert-jws-stmt jwk-id (jose/json-dumps jws)))))))


(defn cache-jws [db payload default-jwk]
  (sqlite/on-serialize
   db (fn [db]
        (sqlite/db-cmd db :run "DELETE FROM jws;")
        (sqlite/db-cmd db :each select-list-jwk-stmt (cache-jws* db payload default-jwk)))))


(defn rotate-keys []
  (sqlite/on-serialize
   (fn [db]
     (let [jwk-es512 (jose/jwk-gen "ES512")
           jwk-ecmr (jose/jwk-gen "ECMR")
           payload (create-payload jwk-es512 jwk-ecmr)
           jws (create-jws payload jwk-es512) ]
       (-> db
           (cache-jws payload jwk-es512)
           (insert-jwk-jws jwk-ecmr)
           (insert-jwk-jws jwk-es512 jws)
           (sqlite/db-cmd :run "VACUUM;"))))))
