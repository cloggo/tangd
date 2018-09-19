(ns app.routes
  (:require
   [async-restify.core :as restify]
   #_[registrar.core :as registrar]))

;; Restify don't support regexp path, only string and wildcard
(def routes
  [[ :get "/adv/"  #(restify/http-request :adv %&) ]
   [ :get "/adv/:kid" #(restify/http-request :adv-kid %&) ]
   #_[ :post "/keys/rotate" #(restify/http-request :keys %&)]
   #_[ :get "/t-error/"  #(restify/http-request :t-error %&)]
   [ :post "/rec/:kid" #(restify/http-request :rec %&)]])
