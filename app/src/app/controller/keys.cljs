(ns app.controller.keys
  (:require
   [re-frame.core :as rf]
   [app.service.keys :as keys]
   [app.coop :as coop]))

(rf/reg-event-fx
 :rotate-keys
 [(coop/context-> :restify)]
 (fn [cfx [_ [req]]]
   {:restify {:payload (keys/rotate-keys)}}))
