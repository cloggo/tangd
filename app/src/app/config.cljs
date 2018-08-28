(ns app.config
  (:require
   [restify.restify :as restify]
   [jose.jwk-formatter :as jwk-formatter]
   [restify.body-parser :as body-parser]
   [jose.jwk-parser :as jwk-parser]
   [restify.transit-formatter :as transit-formatter]))

(def app-name "tangd")

(def port 8080)

(def dbname "./jwkStore.sqlite3")

(def server-options
  #js {:ignoreTrailingSlash true
       :name app-name
       :formatters (js-obj "application/transit+json"
                           transit-formatter/transit-format
                           "application/jwk+json"
                           jwk-formatter/jwk-format)})

(def response-headers #js {:content-type "application/transit+json"})

(body-parser/add-parser! {"application/jwk+json" (fn [_] jwk-parser/jwk-parser)})

(restify/set-response-spec-defaults! {:headers response-headers})
