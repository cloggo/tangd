(ns jose.jwk-formatter
  (:require
   [jose.core :as jose]
   [oops.core :as oops]))

(defn jwk-format [req res body]
  (let [;; _ (println "hello: " body)
        data (when body (jose/json-dumps body jose/default-json-flag))
        len (if data (oops/ocall js/Buffer :byteLength data) 0)]
    (oops/ocall res :setHeader "Content-Length" len)
    data))

