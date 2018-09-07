(ns app.coop
  (:require
   [re-frame.core :as rf]))


(def ^{:dynamic true} *context-receiver* [:dispatch])

(defn ->context? [params]
  (when (vector? params)
    (let [->context (peek params)
          ->context-meta (meta ->context)]
      (when (get ->context-meta :->context) ->context))))

;;>passing :->context from coeffects to effects
(defn pass-context-intercept [context]
  (let [[eventID & params] (get-in context [:coeffects :event])
        params (vec params)
        ->context (->context? params)]
    (if ->context
      ;; passing :->context to effects
      (reduce (fn [context* target-fx]
                (let [params* (get-in context* [:effects target-fx])]
                  (if params*
                    (update-in context* [:effects target-fx]
                               (fn [params*]
                                 #_(println params)
                                 ;; target fx is in *context-receiver*
                                 (let [->context* (->context? params*)]
                                   (if ->context*
                                     ;; merge with existing context
                                     (conj (pop params*) (merge ->context* ->context))
                                     ;; append to end of params
                                     (conj params* ->context)))))
                    ;; target fx is not in *context-receiver*
                    context*)))
              context
              *context-receiver*)
      ;; coeffects does not contains :->context
      context)))

(def context->
  (rf/->interceptor
   :id :pass-context
   :after pass-context-intercept))


(defn restify-context-intercept [context]
  (let [[_ params] (get-in context [:coeffects :event])
        context* (update-in context [:coeffects :event]
                            (fn [event]
                              (conj (pop event) ^{:->context true} {:restify params})))]
    context*))

(def restify-context->
  (rf/->interceptor
   :id :restify-context->
   :before restify-context-intercept))


(defn append-interceptor-event-register [& intercept]
  (fn
    ([id handler] (rf/reg-event-fx id intercept handler))
    ([id interceptor handler] (rf/reg-event-fx id (into interceptor intercept) handler))))

;;<=====================

(defn reg-fx [id fx-handler]
  (set! *context-receiver* (conj *context-receiver* id))
  #_(println *context-receiver*)
  (rf/reg-fx id fx-handler))


(def reg-event-fx
  (append-interceptor-event-register context->))


(def restify-route-event
  (append-interceptor-event-register restify-context-> context->))

;;<==================
