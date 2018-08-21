(ns app.controller.adv
  (:require
   [app.registrar :as registrar]))


(defn respond [kid]
  #js {:hello kid})

(registrar/register-evt :adv :restify {:status :CREATED
                                       :callback [ respond [[:params :kid]]]})

(registrar/register-evt :adv* :restify {:status :CREATED
                                       :callback [ #(identity #js {:hello "world"}) ]})
