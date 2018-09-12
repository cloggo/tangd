(ns app.controller.adv
  (:require
   [app.service.keys :as keys]))


(defn respond [[kid]]
  #js {:hello kid})
