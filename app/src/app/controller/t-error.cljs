(ns app.controller.t-error
  (:require
   [app.coop :as coop]))

(coop/restify-route-event
 :t-error
 (fn [cfx [params]]
   {:restify [{:error "Ooh no" :status :BAD_REQUEST}]}))
