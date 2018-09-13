(ns async.restify.core
  (:require
   #_[async-error.core :refer-macros [go-try <?] :refer [throw-err]]
   [restify.core :as restify]
   [async.core :as async :refer-macros [go <! chan]] ))


(defn run-response [route-name context]
  (let [ch (async/chan)]
    (async/subscribe :http-response route-name ch)
    (async/go
      (let [{:keys [data]} (<! ch)]
        (restify/restify-fx data context)))))


(defn reg-http-request-handler [route-name f]
  (let [ch (async/chan)]
    (async/subscribe :http-request route-name ch)
    (async/go
      (let [{:keys [data]} (<! ch)]
        (run-response route-name data)
        (f data)))))


(defn http-request [route-name context]
    (async/push :http-request route-name context))


(defn http-response [route-name spec]
  (async/push :http-response route-name spec))


(defn init-async-restify []
  (async/reg-handler-class :http-request)
  (async/reg-handler-class :http-response))
