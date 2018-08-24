(ns app.core
  (:require [restify]
            [app.loader]
            [app.lib.transit-parser :as transit-parser]
            [oops.core :as oops] #_[body-parser]
            [goog.array :as garray]
            [cljs.nodejs :as node]
            [app.lib.interop :as interop]
            [app.lib.router :as router]
            [app.routes :as routes]
            [app.config :as config]))

(node/enable-util-print!)

(def server (oops/ocall restify :createServer config/server-options))

#_(oops/ocall server :use (body-parser/body-parser #js {:mapParams true}))
(oops/ocall server :use (transit-parser/body-parser))

(mapv (partial router/register-route server) routes/routes)


(defn -main [& args]
  (oops/ocall server
              :listen config/port
              #(interop/log "%s listening at %s" (.-name server) (.-url server))))

(set! *main-cli-fn* -main)
