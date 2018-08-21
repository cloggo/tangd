(ns app.routes
  (:require [app.controller.adv :as adv]
            [app.controller.rec :as rec]))

;; Restify don't support regexp path, only string and wildcard
(def routes
  [[ :get "/adv/" adv/respond-top ]
   [ :get "/adv/:kid" adv/respond-kid ]
   [ :post "/rec/:kid" rec/respond-kid ]])

