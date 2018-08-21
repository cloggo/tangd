(ns tangd.controller.rec
  (:require
   [tangd.defaults :as defaults]
   [oops.core :as oops]))


(defn respond-kid [req res next]
  (do
    (oops/ocall res :send
                (:CREATED defaults/http-status)
                #js {:kid (oops/oget req :params :kid)
                     :name (oops/oget req :body :name)}
                defaults/res-headers)
    (next false)))

