(ns restify.core
  (:require
   #_[restify-errors :as errors]
   #_[cljs.core]
   [interop.core :as interop]
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
  (if-let [err (get spec :error)]
    (assoc spec
           :next?
           (if (instance? js/Error err)
             err
             (interop/create-error (:status spec) err)))
    spec))

(defn restify-fx [spec]
  #_(println "restify-fx: " spec)
  (let [[spec ->context] spec
        spec (apply-defaults spec)
        spec (apply-status spec)
        spec (apply-error-payload spec)
        {:keys [error payload status headers next? send-mode]} spec
        [req resp next*] (:restify ->context)]
    (when-not error (oops/ocall+ resp send-mode status payload headers))
    (next* next?)))
