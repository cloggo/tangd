(ns jose.jose
  (:require
   [clojure.string :as string]
   [oops.core :as oops]
   [c-jose :as jose] ))

(def default-json-flag
  (let [f0 (oops/oget jose "jose_json_encoding.JSON_SORT_KEYS")
        f1 (oops/oget jose "jose_json_encoding.JSON_COMPACT")]
    (bit-or f0 f1)))

(defn json-dumps
  ([json] (json-dumps json 0))
  ([json flag]
   (oops/ocall jose :jose_json_dumps json flag)))

(defn json-loads [json]
   (oops/ocall jose :jose_json_loads json))

(defn jwk-gen [alg]
  (let [json (json-loads (string/join ["{\"alg\": \"" alg "\"}"]))
        ;; Whoa! modified in place
        _ (oops/ocall jose :jose_jwk_gen json)] json))

(defn get-alg-kind [kind] (oops/oget+ jose :jose_alg_kind kind))

(defn get-alg [kind]
  (let [a (atom [])]
    (oops/ocall jose :jose_alg_foreach
                (fn [k n] (when (= k kind) (swap! a conj n))))
    @a))


(defn calc-thumbprint
  ([jwk] calc-thumbprint jwk "S1")
  ([jwk alg]  (let [dlen (oops/ocall jose :jose_jwk_thp_buf js/undefined alg js/undefined 0)
                    buf (oops/ocall js/Buffer :alloc dlen)
                    olen (oops/ocall jose :jose_jwk_thp_buf jwk alg buf dlen)]
                ;; encode to bas64 no padding (javascript accept no paddding when decode)
                (oops/ocall jose :jose_b64_enc_bbuf buf))))
