(ns restify.transit-formatter
  (:require
   [oops.core :as oops]
   [cognitect.transit :as t]))

(defn transit-format [req res body]
  (let [w (t/writer :json)
        ;; _ (println "hello: " body)
        data (when body (t/write w body))
        len (if data (oops/ocall js/Buffer :byteLength data) 0)]
    (oops/ocall res :setHeader "Content-Length" len)
    data))

