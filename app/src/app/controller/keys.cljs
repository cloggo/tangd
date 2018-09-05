(ns app.controller.keys
  (:require
   [app.service.keys :as keys]
   [restify.core :as restify]))

(restify/reg-event-fx
 :rotate-keys
 (fn [cfx [_ [req]]]
   {:restify {:payload (keys/rotate-keys)}}))
