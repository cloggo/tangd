(ns tangd.core
  (:require [cljs.nodejs :as node]
            [restify]
            [goog.string :as gstring]
            [goog.string.format]
            [oops.core :refer [oget oset! ocall]]))

 ;;(node/enable-util-print!)


;; (def restify (node/require "restify"))

(defn log [& args]
  (.log js/console (apply gstring/format args)))

(defn respond [req res next]
  (do
    (.send res (str "hello " (oget req :params :name)))
    (next false)))

;; (def server (.createServer restify))
(def server (ocall restify :createServer))

(do
  (ocall server :get "/hello/:name" respond)
  (ocall server :head "/hello/:name" respond))

(defn -main [& args]
  (ocall server :listen 8080 #(log "%s listening at %s" (.-name server) (.-url server))))

(set! *main-cli-fn* -main)
