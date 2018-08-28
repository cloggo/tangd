(ns restify.router
  (:require
   [oops.core :as oops]))

(defn register-route [server]
  (fn [[method route callback]] (oops/ocall+ server method route callback)))
