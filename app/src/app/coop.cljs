(ns app.coop
  (:require
   [interop.core :as interop]
   [re-frame.core :as rf]))


(def ^{:dynamic true} *context-receiver* [:dispatch])

(defn ->context? [->context]
  (-> ->context
      (meta)
      (get :->context)
      (when ->context)))


;;>passing :->context from coeffects to effects
(defn pass-context-intercept [context]
  (let [->context (peek (get-in context [:coeffects :event]))]
    (if-let [->context (->context? ->context)]
      ;; passing :->context to effects
      (reduce (fn [context* target-fx]
                (if-let [params* (get-in context* [:effects target-fx])]
                  (assoc-in context* [:effects target-fx]
                            (if-let [->context* (->context? (peek params*))]
                              (conj (pop params*) (merge ->context* ->context))
                              (conj params* ->context)))
                  context*))
              context
              *context-receiver*)
      ;; coeffects does not contains :->context
      context)))

(def context->
  (rf/->interceptor
   :id :pass-context
   :after pass-context-intercept))


(defn restify-route-params? [v]
  (and (seq? v)
       (when (interop/js-type-name? (first v) "IncomingMessage") v)))

(defn restify-context-intercept [context]
  (-> context
      (get-in [:coeffects :event])
      (->>
       (mapv #(if (restify-route-params? %) ^:->context {:restify %} %))
       (assoc-in context [:coeffects :event]))))

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
  (append-interceptor-event-register rf/trim-v context->))


(def restify-route-event
  (append-interceptor-event-register rf/trim-v restify-context-> context->))

;;<==================
