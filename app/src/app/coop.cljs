(ns app.coop
  (:require
   [re-frame.core :as rf]
   [restify.core :as restify]))


(defn pass-context-intercept [target-fx]
  (fn [context]
    (let [[_ from-context] (get-in context [:coeffects :event])]
      (update-in context [:effects target-fx]
                 #(merge {:->context from-context} %)))))

(defn context-> [target-fx]
  (rf/->interceptor
   :id :pass-context
   :after (pass-context-intercept target-fx)))

(rf/reg-fx :restify restify/restify-fx)
