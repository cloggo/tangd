(ns app.controller.adv
  (:require
   [oops.core :as oops]))


(defn respond-top [req res next]
  (do
    (.send res (str "adv: " "top"))
    (next false)))

(defn respond-kid [req res next]
  (do
    (.send res (str "adv: " (oops/oget req :params :kid)))
    (next false)))
