(ns app.lib.restify
  (:require
   [app.config :as config]
   [app.lib.const* :as const]
   [oops.core :as oops]))

(defn send- [res next status data headers next?]
  (do (oops/ocall res :send status data headers) (next next?)))

(defn extract-request [req paths]
  (if (nil? paths) [] (mapv #(oops/oget+ req %) paths)))

(def defaults {:headers config/res-headers
               :next? false})

(defn build-response-object [data]
  (let[data (merge defaults data)
       {headers :headers
        next? :next?
        paths :callback-arg-paths
        extractor :extractor
        status :status
        res-cb :callback} data]
    [(status const/http-status)
     (apply res-cb (extractor paths))
     headers
     next?]))

(defn send [req res next data]
  (let [data (build-response-object (assoc data :extractor (partial extract-request req)))]
    (apply send- res next data)))

