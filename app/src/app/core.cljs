(ns app.core
  (:require
   [sqlite.core :as sqlite]
   [cljs.nodejs :as node]
   [app.config :as config]))

(node/enable-util-print!)

(defn -main [& args]
  (let [[flag0 path] args]
    (sqlite/set-db-name!
     (if (or (= flag0 "--data") (= flag0 "-d"))
       (or path "./keys.sqlite3") "./keys.sqlite3"))
    (config/start-server)))

(set! *main-cli-fn* -main)
