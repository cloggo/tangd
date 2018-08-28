(ns app.routes
  (:require
   [registrar.registrar :as registrar]))

;; Restify don't support regexp path, only string and wildcard
(def routes
  [[ :get "/adv/"  #(registrar/dispatch! :adv* %&) ]
   [ :get "/adv/:kid" #(registrar/dispatch! :adv %&) ]
   [ :post "/keys/rotate" #(registrar/dispatch! :rotate-keys %&) ]
   [ :get "/t-error/" #(registrar/dispatch! :t-error %&) ]
   [ :post "/rec/:kid" #(registrar/dispatch! :rec %&)]])
