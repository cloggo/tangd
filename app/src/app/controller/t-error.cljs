(ns app.controller.t-error
  (:require
   [restify.core :as restify]
   [restify-errors :as errors]
   #_[registrar.core :as registrar]))

;; (registrar/reg-evt :t-error :restify #(identity
;;                                        (errors/BadRequestError. #{:info #{:baz "tada"}}
;;                                                                 "This is an error test.")))


(restify/reg-event-fx
 :t-error
 (fn [cfx [_ [req res]]]
   #_(println res)
   {:restify {:error "Ooh no" :status :BAD_REQUEST}}))
