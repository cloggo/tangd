(ns tangd.core
  (:require [restify]
            [tangd.lib.interop :as interop]
            [tangd.lib.router :as router]
            [tangd.config :as config]
            [oops.core :as oops]))

(def server (oops/ocall restify :createServer))

(mapv (partial router/register-route server) config/routes)


(defn -main [& args]
  (oops/ocall server
              :listen config/port
              #(interop/log "%s listening at %s" (.-name server) (.-url server))))

(set! *main-cli-fn* -main)
