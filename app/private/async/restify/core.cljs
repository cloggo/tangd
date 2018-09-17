(ns async.restify.core
  (:require
   #_[async-error.core :refer-macros [go-try <?] :refer [throw-err]]
   [oops.core :as oops]
   [restify.core :as restify]
   [async.core :as async :refer-macros [go <! chan]] ))


(defn run-response [ch context]
  (async/go
    (let [{:keys [data]} (<! ch)]
      (restify/restify-fx data context))))


(defn reg-http-request-handler [route-key f]
  (let [ch (async/chan)
        req-ch (async/chan)
        res-ch (async/chan)
        route-pub (async/pub ch route-key)]
    (async/reg-route route-key ch route-pub)
    (async/subscribe route-key :http-request req-ch)
    (async/subscribe route-key :http-response res-ch)
    (async/go-loop []
      (let [{:keys [data]} (<! req-ch)]
        (run-response res-ch data)
        (f data))
      (recur))))


(defn http-request [route-key context]
  (async/push route-key :http-request context))


(defn http-response [route-key spec]
  (async/push route-key :http-response spec))


(defn check-error-result [result]
  (if (async/error? result)
    {:status :INTERNAL_SERVER_ERROR :error result}
    result))


(defn acceptable-content? [req content-type]
  (oops/ocall req :is content-type))

