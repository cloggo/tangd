(ns app.loader
  (:require
   [app.controller.keys]))

(defn load-controllers []
  (app.controller.keys/handler))

