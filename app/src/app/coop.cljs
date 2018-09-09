(ns app.coop
  (:require
   #_[oops.core :as oops]
   #_[node-cleanup :as nc]
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
(defn open-db [{:keys [db]} [_ init-stmts]]
  (let [sqlite-db (sqlite/on-db)]
    ;; close sqlite db when node js exit: already done by the library destructor
    ;; (nc (fn [_ _] (sqlite/db-close sqlite-db)))
    (sqlite/init-db sqlite-db init-stmts)
    {:db (assoc-in db [:sqlite :db] sqlite-db)}))


(defn sqlite-cmd-fx [v]
  (let [[spec ->context] v]
    (as-> spec &
      (get & :callback)
      (if & (assoc spec
                   :callback
                   (fn [result] (rf/dispatch [& result ->context])))
          spec)
      (sqlite/cmd-fx &))))

;;<========

(def reg-fx c/reg-fx)

(def reg-event-fx c/reg-event-fx)

