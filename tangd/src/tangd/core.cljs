(ns tangd.core
  (:require [restify]
            ;; [cljs.nodejs :as node]
            [tangd.lib.interop :as interop]
            [tangd.lib.router :as router]
            [tangd.routes :as routes]
            [tangd.config :as config]
            [oops.core :as oops]))

;; (node/enable-util-print!)

(def server (oops/ocall restify :createServer config/server-options))

(oops/ocall server :use
            (.call (oops/oget restify :plugins :bodyParser) restify  #js { :mapParams true}))

(mapv (partial router/register-route server) routes/routes)


(defn -main [& args]
  (oops/ocall server
              :listen config/port
              #(interop/log "%s listening at %s" (.-name server) (.-url server))))

(set! *main-cli-fn* -main)
