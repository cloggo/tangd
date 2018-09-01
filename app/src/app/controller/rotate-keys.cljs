(ns app.controller.rotate-keys
  (:require
   [app.service.keys :as keys]
   [app.config :as config]
   [jose.core :as jose]
   [registrar.core :as registrar]))


(defn respond []
  (keys/rotate-keys config/db-name))

(registrar/reg-evt :rotate-keys :restify respond []
                   {:headers #js {:content-type "application/json"}
                    :status :NO_CONTENT})
