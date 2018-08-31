(ns app.controller.adv
  (:require
   [app.service.keys :as keys]
   [registrar.registrar :as registrar]))


(defn respond [[kid]]
  #js {:hello kid})

(registrar/reg-evt :adv :restify respond [[:params :kid]]
                   {:headers #js {:content-type "application/json"}})

(defn new-jwk [] keys/jwk-ecmr)

(registrar/reg-evt :adv* :restify new-jwk [] {:headers #js {:content-type "application/jwk+json"}})
