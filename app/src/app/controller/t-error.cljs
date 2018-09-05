(ns app.controller.t-error
  (:require
   [app.coop :as coop]
   #_[restify-errors :as errors]
   #_[registrar.core :as registrar]))

;; (registrar/reg-evt :t-error :restify #(identity
;;                                        (errors/BadRequestError. #{:info #{:baz "tada"}}
;;                                                                 "This is an error test.")))


(coop/reg-event-fx
 :t-error
 (fn [cfx [_ [req res]]]
   #_(println res)
   {:restify {:error "Ooh no" :status :BAD_REQUEST}}))
