(ns app.controller.adv
  (:require
   [app.registrar :as registrar]))


(defn respond [[kid]]
  #js {:hello kid})

(registrar/reg-evt :adv :restify respond [[:params :kid]]
                   {:headers #js {:content-type "application/json"}})

(registrar/reg-evt :adv* :restify
                   #(identity #js {:hello "world"}))
