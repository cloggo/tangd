(ns app.routes
  (:require
   [re-frame.core :as rf]
   #_[registrar.core :as registrar]))

;; Restify don't support regexp path, only string and wildcard
(def routes
  [#_[ :get "/adv/"  #(registrar/dispatch! :adv* %&) ]
   #_[ :get "/adv/:kid" #(registrar/dispatch! :adv %&) ]
   #_[ :post "/keys/rotate" #(registrar/dispatch! :rotate-keys %&) ]
   [ :get "/t-error/" #(rf/dispatch [:t-error %&]) ]
   #_[ :post "/rec/:kid" #(registrar/dispatch! :rec %&)]])
