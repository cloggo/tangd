(ns app.controller.rec
  (:require
   [app.registrar :as registrar]))

(defn rec-response [kid name]
  #js {:kid kid :name name})

(registrar/reg-evt :rec :restify {:status :CREATED
                                  :callback [rec-response [[:params :kid] [:body :name]]]})
