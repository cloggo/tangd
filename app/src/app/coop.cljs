(ns app.coop
  (:require
   [re-frame.core :as rf]
   [restify.core :as restify]))


(def ^{:dynamic true} *context-receiver* [])

;;>customized event register
(defn pass-context-intercept [context]
  (let [[_ data] (get-in context [:coeffects :event])]
    (if data
      (reduce (fn [context target-fx]
                (if (get-in context [:effects target-fx])
                  (update-in context
                             [:effects target-fx :->context]
                             #(merge % data))
                  context))
              context
              *context-receiver*)
      context)))

(def context->
  (rf/->interceptor
   :id :pass-context
   :after pass-context-intercept))


(defn reg-event-fx
  ([id handler] (rf/reg-event-fx id [context->] handler))
  ([id interceptor handler] (rf/reg-event-fx id (conj interceptor context->) handler)))

;;<=====================

(defn reg-fx [id fx-handler]
  (set! *context-receiver* (conj *context-receiver* id))
  (rf/reg-fx id fx-handler))

(reg-fx :restify restify/restify-fx)

;;<==================
