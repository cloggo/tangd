(ns app.controller.t-error
  (:require
   [restify-errors :as errors]
   [registrar.core :as registrar]))

(registrar/reg-evt :t-error :restify #(identity
                                       (errors/BadRequestError. #{:info #{:baz "tada"}}
                                                                "This is an error test.")))
