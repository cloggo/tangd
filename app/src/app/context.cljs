(ns app.context
  (:require
   [re-frame.core :as rf]))

(def ^:dynamic *context-receiver* [:dispatch])

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
                              (conj (pop params*) (merge-with merge ->context* ->context))
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


(defn assoc-meta-context-to-event [key val-creator]
  (fn [context]
    (let [event (get-in context [:coeffects :events])]
      (assoc-in context [:coeffects :events]
                (if-let [->context (->context? (peek event))]
                  (conj (pop event) (assoc ->context key (val-creator)))
                  (conj event ^:->context {key (val-creator)}))))))


(defn append-interceptor-event-register [& intercept]
  (fn
    ([id handler] (rf/reg-event-fx id intercept handler))
    ([id interceptor handler] (rf/reg-event-fx id (into interceptor intercept) handler))))


(defn reg-fx [id fx-handler]
  (set! *context-receiver* (conj *context-receiver* id))
  #_(println *context-receiver*)
  (rf/reg-fx id fx-handler))

(def reg-event-fx
  (append-interceptor-event-register rf/trim-v context->))

