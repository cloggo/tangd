(ns async.restify.core
  (:require
   #_[async-error.core :refer-macros [go-try <?] :refer [throw-err]]
   [restify.core :as restify]
   [async.core :as async :refer-macros [go <! chan]] ))


(defn run-response [handler-class context]
  (let [ch (async/chan)]
    (async/subscribe :http-response handler-class ch)
    (async/go
      (let [{:keys [data]} (<! ch)]
        (restify/restify-fx data context)))))


(defn reg-http-request-handler [handler-class f]
  (let [ch (async/chan)]
    (async/subscribe :http-request handler-class ch)
    (async/go
      (let [{:keys [data]} (<! ch)]
        (run-response handler-class data)
        (f data)))))


(defn http-request [handler-class context]
    (async/push :http-request handler-class context))


(defn http-response [handler-class spec]
  (async/push :http-response handler-class spec))


(defn init-async-restify []
  (async/reg-route :http-request)
  (async/reg-route :http-response))
