(ns async.restify.core
  (:require
   #_[async-error.core :refer-macros [go-try <?] :refer [throw-err]]
   [restify.core :as restify]
   [clojure.core.async :as async] ))


(defn handle-route [handler]
  (let [ch (async/chan)]
    (fn [& restify-context]
      (async/put! ch restify-context)
      (async/go
        (-> (handler ch)
            (<!) (restify/restify-fx restify-context))))))
