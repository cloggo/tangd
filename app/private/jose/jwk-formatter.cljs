(ns jose.jwk-formatter
  (:require
   [jose.jose :as jose]
   [oops.core :as oops]))

(defn jwk-format [req res body]
  (let [;; _ (println "hello: " body)
        data (if body (jose/json-dumps body jose/default-json-flag) nil)
        len (if data (oops/ocall js/Buffer :byteLength data) 0)]
    (oops/ocall res :setHeader "Content-Length" len)
    data))

