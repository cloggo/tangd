(ns app.lib.restify
  (:require
   [app.registrar :as registrar]
   [app.config :as config]
   [app.lib.const* :as const]
   [oops.core :as oops]))

(defn extract-request [req paths]
  (mapv #(oops/oget+ req %) paths))

(defn build-response-object [data]
  (let[data (merge config/response-defaults data)
       {headers :headers
        next? :next?
        extractor :extractor
        status :status
        cb :callback} data
       [cb-fn paths] cb]
    [(status const/http-status)
     (apply cb-fn (extractor paths))
     headers
     next?]))

(defn send- [res next status data headers next?]
  (do (oops/ocall res :send status data headers) (next next?)))

(defn respond [data req res next]
  (let [data (build-response-object (assoc data :extractor (partial extract-request req)))]
    (apply send- res next data)))


(registrar/reg-fx :restify respond)
