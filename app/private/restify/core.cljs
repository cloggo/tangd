(ns restify.core
  (:require
   [re-frame.core :as rf]
   #_[restify-errors :as errors]
   [cljs.core]
   [restify.const* :as const]
   [oops.core :as oops]))


;; defaults restify spec
;; =====================
(def ^{:dynamic true :private true} *response-spec-defaults* {:status :OK
                                                :send-mode :send
                                                :next? false})


(defn add-response-spec-defaults! [spec]
  (set! *response-spec-defaults* (merge *response-spec-defaults* spec)))

(defn apply-defaults [spec]
  (merge *response-spec-defaults* spec))


;; get status code from status key
;; ========
(defn apply-status [spec]
  (assoc spec :status ((:status spec) const/http-status)))

;; =================

(defn apply-error-payload [spec]
  (let [err (get spec :error)
        headers (:headers spec)]
    (if err (assoc spec :next?
                   (-> (js/Error.)
                       (oops/oset! "!info" err)
                       (oops/oset! "!statusCode" (:status spec))))
        spec)))


(defn restify-fx [spec]
  (let [spec (apply-defaults spec)
        spec (apply-status spec)
        spec (apply-error-payload spec)
        {:keys [response payload status headers next* next? send-mode]} spec]
    (when-not (get spec :error) (oops/ocall+ response send-mode status payload headers))
    (next* next?)))

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

(rf/reg-fx :restify restify-fx)
