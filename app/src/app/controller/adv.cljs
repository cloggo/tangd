(ns app.controller.adv
  (:require
   [app.registrar :as registrar]))


(defn respond [kid]
  #js {:hello kid})

(registrar/reg-evt :adv :restify {:status :OK
                                  :callback [ respond [[:params :kid]]]})

(registrar/reg-evt :adv* :restify {:status :OK
                                   :callback [ #(identity #js {:hello "world"}) ]})
