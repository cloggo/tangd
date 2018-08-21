(ns app.routes
  (:require [app.registrar :as registrar]
            [app.controller.adv :as adv]
            [app.controller.rec :as rec]))

;; Restify don't support regexp path, only string and wildcard
(def routes
  [[ :get "/adv/"  #(apply registrar/dispatch! :adv* %&) ]
   [ :get "/adv/:kid" #(apply registrar/dispatch! :adv %&) ]
   [ :post "/rec/:kid" #(apply registrar/dispatch! :rec %&)]])
