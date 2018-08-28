(ns restify.transit-parser
  (:require [cognitect.transit :as t]
            [oops.core :as oops]))

(defn transit-parser [req, res, next]
  (let [r (t/reader :json)
        body (oops/oget req :body)]
    (oops/oset! req "!_body" body)
    (oops/oset! req "!rawBody" body)
    (oops/oset! req "!isTransit" true)
    (oops/oset! req :body (t/read r (oops/ocall body :toString)))
    #_(println "transit: " (oops/oget req :body))
    (next)))
