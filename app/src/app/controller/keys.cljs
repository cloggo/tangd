(ns app.controller.keys
  (:require
   [app.service.keys :as keys]
   [app.coop :as coop]))

(coop/restify-route-event
 :rotate-keys
 keys/rotate-keys)
