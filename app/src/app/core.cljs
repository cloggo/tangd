(ns app.core
  (:require
   [cljs.nodejs :as node]
   [app.config :as config]))

(node/enable-util-print!)

(defn -main [& args]
  (config/start-server config/config))

(set! *main-cli-fn* -main)
