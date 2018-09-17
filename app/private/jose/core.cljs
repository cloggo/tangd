(ns jose.core
  (:require
   [clojure.string :as string]
   [oops.core :as oops]
   [c-jose :as jose] ))

;;> JSON operations
(def default-json-flag
  (let [f0 (oops/oget jose "jose_json_encoding.JSON_SORT_KEYS")
        f1 (oops/oget jose "jose_json_encoding.JSON_COMPACT")]
    (bit-or f0 f1)))

(defn json-dumps
  ([json] (json-dumps json default-json-flag))
  ([json flag]
   (oops/ocall jose :jose_json_dumps json flag)))

(defn json-loads [json]
   (oops/ocall jose :jose_json_loads json))

(defn json-get [json key]
  (oops/ocall jose :jose_json_get json key))

(defn json-value-get [json]
  (oops/ocall jose :jose_json_value_get json))

(defn json-object-update [json other]
  (oops/ocall jose :jose_json_object_update json other))


(defn json-copy [json]
  (oops/ocall jose :jose_json_deep_copy json))

(defn json-array-append [arr & vals]
  (mapv #(oops/ocall jose :jose_json_array_append arr %) vals) arr)

(defn json-array [& [vals]]
  (let [arr (oops/ocall jose :jose_json_array)]
    (apply json-array-append arr vals)))

(defn jwks->keys [& jwks]
  (let [jwks (mapv json-dumps jwks)
        jwks (string/join ["{\"keys\": [" (string/join "," jwks) "]}"])]
    jwks))


;;< ===================


;;> base64 operations
(defn b64-enc-sbuf [s]
  (oops/ocall jose :jose_b64_enc_sbuf s))

(defn b64-dec-sbuf [s]
  (oops/ocall jose :jose_b64_dec_buf s))

;;< ====================



;;> jwk operations
(defn jwk-gen [alg]
  (let [json (json-loads (string/join ["{\"alg\": \"" alg "\"}"]))
        ;; Whoa! modified in place
        _ (oops/ocall jose :jose_jwk_gen json)] json))

(defn jwk-pub [jwk]
  (let [jwk* (oops/ocall jose :jose_json_deep_copy jwk)]
    (oops/ocall jose :jose_jwk_pub jwk*) jwk*))

(defn jwk-prm [jwk req op]
  (oops/ocall jose :jose_jwk_prm jwk req op))

(defn jws-sig [payload sig jwk]
  (let [pb64  (b64-enc-sbuf payload)
        jws (json-loads (string/join ["{\"payload\":\"" pb64 "\"}"]) )]
    (oops/ocall jose :jose_jws_sig jws sig jwk)
    jws))

(defn get-alg-kind [kind] (oops/oget+ jose :jose_alg_kind kind))

(defn get-alg [kind]
  (let [a (atom [])]
    (oops/ocall jose :jose_alg_foreach
                (fn [k n] (when (= k kind) (swap! a conj n))))
    @a))


(defn jwk-exc [lcl rem]
  (oops/ocall jose :jose_jwk_exc lcl rem))


(defn calc-thumbprint
  ([jwk] (calc-thumbprint jwk "S1"))
  ([jwk alg]  (let [dlen (oops/ocall jose :jose_jwk_thp_buf js/undefined alg js/undefined 0)
                    buf (oops/ocall js/Buffer :alloc dlen)
                    olen (oops/ocall jose :jose_jwk_thp_buf jwk alg buf dlen)]
                ;; encode to bas64 no padding (javascript accept no paddding when decode)
                (oops/ocall jose :jose_b64_enc_bbuf buf))))

;;< =======================
