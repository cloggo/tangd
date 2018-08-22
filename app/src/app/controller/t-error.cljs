(ns app.controller.t-error
  (:require
   [restify-errors :as errors]
   [app.registrar :as registrar]))

(registrar/reg-evt :t-error :restify #(identity (errors/BadRequestError.)))
