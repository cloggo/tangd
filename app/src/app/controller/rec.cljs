(ns app.controller.rec
  (:require
   [app.lib.const* :as const]
   [app.config :as config]
   [oops.core :as oops]))


(defn respond-kid [req res next]
  (do
    (oops/ocall res :send
                (:CREATED const/http-status)
                #js {:kid (oops/oget req :params :kid)
                     :name (oops/oget req :body :name)}
                config/res-headers)
    (next false)))

