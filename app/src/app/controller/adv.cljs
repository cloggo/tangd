(ns app.controller.adv
  (:require
   [c-jose :as jose]
   [oops.core :as oops]
   [app.registrar :as registrar]))


(defn respond [[kid]]
  #js {:hello kid})

(registrar/reg-evt :adv :restify respond [[:params :kid]]
                   {:headers #js {:content-type "application/json"}})

(defn new-jwk []
  (let [f0 (oops/oget jose "jose_json_encoding.JSON_SORT_KEYS")
        f1 (oops/oget jose "jose_json_encoding.JSON_COMPACT")
        jwk (oops/ocall jose :jose_json_loads "{\"alg\": \"ES512\"}")
        _ (oops/ocall jose :jose_jwk_gen jwk)]
    (oops/ocall jose :jose_json_dumps jwk (bit-or f0 f1))))


(registrar/reg-evt :adv* :restify new-jwk [] {:headers #js {:content-type "application/json"}
                                              :send-mode :sendRaw})
