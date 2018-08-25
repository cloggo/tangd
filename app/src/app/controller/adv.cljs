(ns app.controller.adv
  (:require
   [app.lib.jose :as jose]
   [app.registrar :as registrar]))


(defn respond [[kid]]
  #js {:hello kid})

(registrar/reg-evt :adv :restify respond [[:params :kid]]
                   {:headers #js {:content-type "application/json"}})

(defn new-jwk [] jose/jwk-ecmr)

(registrar/reg-evt :adv* :restify new-jwk [] {:headers #js {:content-type "application/jwk+json"}})
