(ns restify.core
  (:require
   #_[restify-errors :as errors]
   #_[cljs.core]
   [interop.core :as interop]
   [restify.const* :as const]
   [oops.core :as oops]))


;; defaults restify spec
;; =====================
(def ^{:dynamic true :private true} *response-spec-defaults*
  {:status :OK
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


(defn create-error [status info]
  (-> (js/Error.)
      (oops/oset! "!info" info)
      (oops/oset! "!statusCode" (status const/http-status))))

;; =================

(defn apply-error-payload [spec]
  (if-let [err (get spec :error)]
    (assoc spec
           :next?
           (if (instance? js/Error err)
             err
             (create-error (:status spec) err)))
    spec))


(defn restify-fx [spec ->context]
  #_(println "restify-fx: " spec)
  (let [spec (apply-defaults spec)
        spec (apply-error-payload spec)
        spec (apply-status spec)
        {:keys [error payload status headers next? send-mode]} spec
        [req resp next*] ->context]
    (when-not error (oops/ocall+ resp send-mode status payload headers))
    (next* next?)))


(defn restify-fx* [spec]
  "designed for re-frame"
  (let [[spec ->context] spec
        ->context (:restify ->context)]
    (restify-fx spec ->context)))

