(ns app.routes
  (:require
   [async.restify.core :as restify]
   #_[registrar.core :as registrar]))

;; Restify don't support regexp path, only string and wildcard
(def routes
  [[ :get "/adv/"  #(restify/http-request :adv %&) ]
   [ :get "/adv/:kid" #(restify/http-request :adv-kid %&) ]
   [ :post "/keys/rotate" #(restify/http-request :keys %&)]
   [ :get "/t-error/"  #(restify/http-request :t-error %&)]
   #_[ :post "/rec/:kid" #(registrar/dispatch! :rec %&)]])
