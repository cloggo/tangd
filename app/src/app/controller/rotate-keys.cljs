(ns app.controller.rotate-keys
  (:require
   [app.service.sqlite :as sqlite]
   [app.config :as config]
   [app.lib.jose :as jose]
   [app.registrar :as registrar]))


(defn respond []
  (sqlite/rotate-keys config/dbname))

(registrar/reg-evt :rotate-keys :restify respond []
                   {:headers #js {:content-type "application/json"}
                    :status :NO_CONTENT})
