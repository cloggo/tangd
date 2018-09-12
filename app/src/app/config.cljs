(ns app.config
  (:require
   #_[registrar.core :as registrar]
   [jose.jwk-formatter :as jwk-formatter]
   [jose.jwk-parser :as jwk-parser]
   [restify.core :as restify]
   [sqlite.core :as sqlite]
   [app.service.schema :as schema]
   [restify.body-parser :as body-parser]
   [restify.transit-formatter :as transit-formatter]))

;;> restify configurations
(def app-name "tangd")

(def port 8080)

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

;;> sqlite initializations

(def db-name "./jwk_keys.sqlite3")

(sqlite/set-db-name! db-name)
;;(rf/dispatch [:open-sqlite-db schema/init-stmts])
(sqlite/init-db (sqlite/on-db) schema/init-stmts)

;;< ========================
