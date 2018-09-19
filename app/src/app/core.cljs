(ns app.core
  (:require
   [sqlite.core :as sqlite]
   [cljs.nodejs :as node]
   [app.config :as config]))

(node/enable-util-print!)

(defn -main [& args]
  (config/arg-parse args)
  (config/start-server))

(set! *main-cli-fn* -main)
