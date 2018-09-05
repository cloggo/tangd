(ns app.controller.keys
  (:require
   [app.service.keys :as keys]
   [app.coop :as coop]))

(coop/restify-event
 :rotate-keys
 (fn [cfx [_ [req]]]
   {:restify {:payload (keys/rotate-keys)}}))
