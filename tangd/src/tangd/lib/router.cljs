(ns tangd.lib.router
  (:require
   [tangd.lib.interop :as interop :refer [ometh]]
   [oops.core :refer [oget oset! ocall]]))

(defn register-route [server r]
  (let [[method & args] r]
    (apply (ometh server method) args)))
