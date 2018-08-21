(ns app.routes
  (:require
   [app.registrar :as registrar]))

;; Restify don't support regexp path, only string and wildcard
(def routes
  [[ :get "/adv/"  #(registrar/dispatch! :adv* %&) ]
   [ :get "/adv/:kid" #(registrar/dispatch! :adv %&) ]
   [ :post "/rec/:kid" #(registrar/dispatch! :rec %&)]])
