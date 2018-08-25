(ns app.lib.jwk-parser
  (:require [app.lib.jose :as jose]
            [oops.core :as oops]))

(defn jwk-parser [req, res, next]
  (let [body (oops/oget req :body)]
    (oops/oset! req "!_body" body)
    (oops/oset! req "!rawBody" body)
    (oops/oset! req "!isJWK" true)
    (oops/oset! req :body (jose/json-loads (oops/ocall body :toString)))
    #_(println "transit: " (oops/oget req :body))
    (next)))
