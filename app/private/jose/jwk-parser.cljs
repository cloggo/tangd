(ns jose.jwk-parser
  (:require [jose.core :as jose]
            [oops.core :as oops]))

(defn jwk-parser [req, res, next]
  (let [body (oops/oget req :body)]
    (oops/oset! req "!_body" body)
    (oops/oset! req "!rawBody" body)
    (oops/oset! req "!isJWK" true)
    #_(println "jwk-parser: " body)
    (oops/oset! req :body (jose/json-loads (oops/ocall body :toString)))
    #_(println "transit: " (oops/oget req :body))
    (next)))
