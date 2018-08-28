(ns app.controller.rotate-keys
  (:require
   [app.service.sqlite :as sqlite]
   [app.config :as config]
   [jose.jose :as jose]
   [registrar.registrar :as registrar]))


(defn respond []
  (sqlite/rotate-keys config/dbname))

(registrar/reg-evt :rotate-keys :restify respond []
                   {:headers #js {:content-type "application/json"}
                    :status :NO_CONTENT})
