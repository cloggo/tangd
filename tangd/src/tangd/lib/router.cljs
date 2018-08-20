(ns tangd.lib.router
  (:require
   [tangd.lib.interop :as interop]))


(defn register-route [server r]
  (let [[method & args] r]
    (apply (interop/ometh server method) args)))
