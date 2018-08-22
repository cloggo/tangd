(ns app.controller.adv
  (:require
   [app.registrar :as registrar]))


(defn respond [[kid]]
  #js {:hello kid})

(registrar/reg-evt :adv :restify-response respond [[:params :kid]])

(registrar/reg-evt :adv* :restify-response
                   #(identity #js {:hello "world"}))
