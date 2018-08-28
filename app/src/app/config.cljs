(ns app.config
  (:require
   [registrar.registrar :as registrar]
   [jose.jwk-formatter :as jwk-formatter]
   [jose.jwk-parser :as jwk-parser]
   [restify.restify :as restify]
   [restify.body-parser :as body-parser]
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

(body-parser/add-parser! {"application/jwk+json" (fn [_] jwk-parser/jwk-parser)})

(def response-headers #js {:content-type "application/transit+json"})

(restify/add-response-spec-defaults! {:headers response-headers})

(apply registrar/reg-fx :restify restify/registrar-params)
