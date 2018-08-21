(ns tangd.controller.rec
  (:require
   [tangd.const* :as const]
   [tangd.config :as config]
   [oops.core :as oops]))


(defn respond-kid [req res next]
  (do
    (oops/ocall res :send
                (:CREATED const/http-status)
                #js {:kid (oops/oget req :params :kid)
                     :name (oops/oget req :body :name)}
                config/res-headers)
    (next false)))

