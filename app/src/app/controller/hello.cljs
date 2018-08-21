(ns app.controller.hello
  (:require
   [oops.core :as oops]))


(defn respond [req res next]
  (do
    (.send res (str "hello " (oops/oget req :params :name)))
    (next false)))
