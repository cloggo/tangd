(ns app.routes
  (:require
   [app.controller.keys :as keys]
   [app.controller.t-error :as t-error]
   #_[registrar.core :as registrar]))

;; Restify don't support regexp path, only string and wildcard
(def routes
  [#_[ :get "/adv/"  #(registrar/dispatch! :adv* %&) ]
   #_[ :get "/adv/:kid" #(registrar/dispatch! :adv %&) ]
   [ :post "/keys/rotate" keys/handler]
   [ :get "/t-error/" t-error/handler ]
   #_[ :post "/rec/:kid" #(registrar/dispatch! :rec %&)]])
