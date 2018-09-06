(ns app.coop
  (:require
   [re-frame.core :as rf]))


(def ^{:dynamic true} *context-receiver* [])

(defn ->context? [params]
  (when (vector? params)
    (let [->context (peek params)
          ->context-meta (meta ->context)]
      (when (get ->context-meta :->context) ->context))))

(defn update->context [->context context target-fx]
  (let [params* (get-in context [:effects target-fx])]
    (if params*
      (update-in context [:effects target-fx]
                 (fn [params**]
                   (let [->context* (->context? params**)]
                     (if ->context*
                       (conj (pop params**) (merge ->context* ->context))
                       (conj params** ->context)))))
      context)))

;;>customized event register
(defn pass-context-intercept [context]
  (let [[_ params] (get-in context [:coeffects :event])
        ->context (->context? params)]
    (if ->context
      (reduce (fn [context target-fx]
                (update->context ->context context target-fx))
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


;;<==================
