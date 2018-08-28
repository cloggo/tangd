(ns interop.router
  (:require
   [interop.interop :as interop]))


(defn register-route [server r]
  (let [[method & args] r]
    (apply (interop/ometh server method) args)))
