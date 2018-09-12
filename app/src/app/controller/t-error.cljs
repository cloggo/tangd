(ns app.controller.t-error
  (:require
   [async-error.core :refer-macros [go-try <?]]
   [async.core :as async*]
   [async.restify.core :as r]))

(def handler (r/handle-route
              (fn [ch]
                (go-try
                 {:error "Ooh no" :status :BAD_REQUEST}))))
