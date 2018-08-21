(ns tangd.controller.rec
  (:require
   [oops.core :as oops]))


(defn respond-kid [req res next]
  (do
    (oops/oset! res "!contentType" "json")
    (oops/ocall res :send #js {:kid (oops/oget req :params :kid)
                               :name (oops/oget req :body :name)})
    (next false)))

