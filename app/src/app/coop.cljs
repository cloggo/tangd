(ns app.coop
  (:require
   #_[oops.core :as oops]
   [sqlite.core :as sqlite]
   [app.context :as c]
   [interop.core :as interop]
   [re-frame.core :as rf]))


;;> restify

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

(def restify-route-event
  (c/append-interceptor-event-register rf/trim-v restify-context-> c/context->))

;;<========

;;> sqlite

;; to be used with re-frame
(defn open-db [{:keys [db]}]
  (let [sqlite-db (sqlite/on-db)]
    ;; close sqlite db when node js exit
    (.on js/process "exit" (fn [_] (sqlite/db-close sqlite-db)))
    {:db (assoc-in db [:sqlite :db] sqlite-db)}))

(defn init-db [{:keys [db]} [_ init-stmts]]
  (sqlite/init-db (get-in db [:sqlite :db]) init-stmts)
  {})


;;<========

(def reg-fx c/reg-fx)

(def reg-event-fx c/reg-event-fx)

