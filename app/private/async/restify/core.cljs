(ns async.restify.core
  (:require
   #_[async-error.core :refer-macros [go-try <?] :refer [throw-err]]
   [restify.core :as restify]
   [async.core :as async] ))


(defn handle-route [handler]
  "Forcing handler to use async because we are expecting the handler to return
   a channel: this is purposely enforced so that async database access will be
   completed before we respond with restify"
  (let [ch (async/chan)]
    (fn [& restify-context]
      (async/put! ch restify-context)

      (async/go
        (-> (handler ch)
            (<!) (restify/restify-fx restify-context))))))
