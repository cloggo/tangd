(ns tangd.core
  (:require [cljs.nodejs :as node]
            [goog.string :as gstring]
            [goog.string.format]
            [oops.core :refer [oget oset!]]))

(node/enable-util-print!)


(def restify (node/require "restify"))

(defn log [& args]
  (println (apply gstring/format args)))

(defn respond [req res next]
  (.send res (str "hello " (oget req :params :name))))

(def server (.createServer restify))

(do
  (.get server "/hello/:name" respond)
  (.head server "/hello/:name" respond))

(defn -main [& args]
  (.listen server 8080 #(log "%s listening at %s" (.-name server) (.-url server))))

(set! *main-cli-fn* -main)
