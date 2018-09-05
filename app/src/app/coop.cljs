(ns app.coop
  (:require
   [re-frame.core :as rf]
   [restify.core :as restify]))


;;>customized event register
(defn pass-context-intercept [target-fx]
  (fn [context]
    (let [[_ from-context] (get-in context [:coeffects :event])]
      (update-in context [:effects target-fx]
                 #(merge {:->context from-context} %)))))

(defn context-> [target-fx]
  (rf/->interceptor
   :id :pass-context
   :after (pass-context-intercept target-fx)))

(defn reg-event-fx [target-fx]
  (let [pass-context (context-> target-fx)]
    (fn
      ([id h] (rf/reg-event-fx id [pass-response] h))
      ([id interceptor h] (rf/reg-event-fx id (conj interceptor pass-response) h)))))

;;<=====================


;;>restify events

(def restify-event (reg-event-fx :restify))

(rf/reg-fx :restify restify/restify-fx)

;;<==================
