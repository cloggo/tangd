(ns app.config
  (:require
   [restify]
   [app.routes :as routes]
   [restify.router :as router]
   [jose.jwk-formatter :as jwk-formatter]
   [jose.jwk-parser :as jwk-parser]
   [restify.core :as restify*]
   [async-restify.core :as async-restify]
   [oops.core :as oops]
   [interop.core :as interop]
   [sqlite.core :as sqlite]
   [app.service.schema :as schema]
   [app.controller.keys :as keys]
   [app.service.keys :as keys*]
   [restify.body-parser :as body-parser]
   [app.service.default-jws :as default-jws]
   [restify.transit-formatter :as transit-formatter]))

;;> app configurations

(def app-name "tangd")

(def ^:dynamic *port* 8080)

(def ^:dyanmic *db-name* "./keys.sqlite3")

(def ^:dynamic *post-db-init* identity)

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

(defn get-restify-config []
  {:options server-options
   :port *port*
   :parser (body-parser/body-parser #js {:mapParams false})
   :routes routes/routes})


(defn init-restify []

  (body-parser/add-parser! extra-parsers)

  (restify*/add-response-spec-defaults! {:headers response-headers}))

;;> sqlite initializations

(defn init-sqlite [db-name]
  (sqlite/set-db-name! db-name)
  (sqlite/init-db (sqlite/on-db) schema/init-stmts *post-db-init*)
  (default-jws/cache-default-jws (sqlite/on-db)))

;;< ========================
(defn start-server []
  (init-sqlite *db-name*)
  (init-restify)

  (let [{:keys [options parser routes port]} (get-restify-config)
        server (oops/ocall restify :createServer options)]

    (oops/ocall server :use parser)

    (mapv (router/register-route server) routes)

    (oops/ocall server
                :listen port
                #(interop/logf "%s listening at %s"
                               (oops/oget server :name)
                               (oops/oget server :url)))))

(defn rotate-and-exit [db-name]
  (set! *db-name* db-name)
  (set! *post-db-init* keys/rotate-and-exit))


(def arg-deck
  [[["--data" "-d"] #(set! *db-name* %)]
   [["rotate-keys"] rotate-and-exit]
   [["--port" "-p"] #(set! *port* %)]])

(defn match-opts [[opt val]]
  (mapv (fn [[opts handler]]
          (when (some #(= % opt) opts) (handler val))) arg-deck))

(defn arg-parse [args]
  (let [args (partition 2 args)]
    (mapv match-opts args)))
