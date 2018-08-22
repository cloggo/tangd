(ns app.controller.rec
  (:require
   [app.registrar :as registrar]))

(defn rec-response [[kid name] res-spec]
  #js {:kid kid :name name})

(registrar/reg-evt :rec :restify-response
                   rec-response
                   [[:params :kid] [:body :name]] {:status :CREATED})
