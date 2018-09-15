(ns app.controller.t-error
  (:require
   [async.restify.core :as restify]))


(restify/reg-http-request-handler
 :t-error
 (fn [context]
   (restify/http-response :t-error
                          {:error "oh no" :status :BAD_REQUEST})))

