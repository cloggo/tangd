(ns app.lib.restify
  (:require
   [app.lib.interop :as interop]
   [app.registrar :as registrar]
   [app.config :as config]
   [app.lib.const* :as const]
   [oops.core :as oops]))

(defn extract-request [paths res-spec [req]]
  {:data (mapv #(oops/oget+ req %) paths)})

(defn apply-defaults [_ res-spec _]
  {:res-spec (merge config/response-defaults res-spec)})

(defn apply-status [_ res-spec _]
  (let [{:keys [status]}  res-spec]
    {:res-spec (assoc res-spec :status (status const/http-status))}))

(defn send- [res next* status data headers next?]
  (do (oops/ocall res :send status data headers) (next* next?)))

(defn respond [data res-spec dispatch-data]
  (let [[req res next*] dispatch-data
        {:keys [status headers next?]} res-spec]
    (send- res next* status data headers next?)))

(registrar/reg-fx :restify-response respond [extract-request] [apply-defaults apply-status])
