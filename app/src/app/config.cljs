(ns app.config
  (:require
   [restify]
   [app.loader]
   [app.routes :as routes]
   [restify.router :as router]
   [jose.jwk-formatter :as jwk-formatter]
   [jose.jwk-parser :as jwk-parser]
   [restify.core :as restify*]
   [async.restify.core :as async-restify]
   [oops.core :as oops]
   [interop.core :as interop]
   [sqlite.core :as sqlite]
   [app.service.schema :as schema]
   [app.controller.keys :as keys]
   [restify.body-parser :as body-parser]
   [app.service.default-jws :as default-jws]
   [restify.transit-formatter :as transit-formatter]))

;;> app configurations

(def app-name "tangd")

;; (def *db-name* "./jwk_keys.sqlite3")

(def port 8080)

(def response-headers #js {:content-type "application/transit+json"})

(def formatters
  (js-obj "application/transit+json"
          transit-formatter/transit-format
          "application/jwk+json"
          jwk-formatter/jwk-format))

(def extra-parsers {"application/jwk+json" (fn [_] jwk-parser/jwk-parser)})


(def server-options
  #js {:ignoreTrailingSlash true
       :name app-name
       :formatters formatters})

;;< ==============================

(def config
  {:options server-options
   :port port
   :parser (body-parser/body-parser #js {:mapParams false})
   :routes routes/routes})


(defn init-restify []

  (body-parser/add-parser! extra-parsers)

  (restify*/add-response-spec-defaults! {:headers response-headers}))

;;> sqlite initializations

(defn init-sqlite []
  ;;(sqlite/set-db-name! db-name)
  ;;(rf/dispatch [:open-sqlite-db schema/init-stmts])
  (sqlite/init-db (sqlite/on-db) schema/init-stmts)
  (default-jws/cache-default-jws (sqlite/on-db)))

;;< ========================


(defn start-server [config]
  (let [{:keys [options parser routes port]} config
        server (oops/ocall restify :createServer options)]

    (init-sqlite)

    (init-restify)

    (oops/ocall server :use parser)

    (mapv (router/register-route server) routes)

    (oops/ocall server
                :listen port
                #(interop/logf "%s listening at %s" (.-name server) (.-url server)))))
