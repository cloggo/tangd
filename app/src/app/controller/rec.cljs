(ns app.controller.rec
  (:require
   [app.lib.restify :as restify]))

(defn rec-response [kid name]
  #js {:kid kid :name name})

(defn respond-kid [req res next]
  (restify/send req res next
         {:status :CREATED
          :callback [rec-response [[:params :kid] [:body :name]]]}))
