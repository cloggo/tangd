(ns app.coop
  (:require
   [re-frame.core :as rf]
   [restify.core :as restify]))


(defn pass-response-intercept [context]
  (let [[_ [req res next*]] (get-in context [:coeffects :event])
        handler-data (get-in context [:effects :restify])]
    (update-in context [:effects :restify]
               #(merge {:response res :next* next*} %))))


(def pass-response
  (rf/->interceptor
   :id :pass-response
   :after pass-response-intercept))


(defn reg-event-fx
  ([id h] (rf/reg-event-fx id [pass-response] h))
  ([id interceptor h] (rf/reg-event-fx id (conj interceptor pass-response) h)))

(rf/reg-fx :restify restify/restify-fx)
