(ns async.core
  (:require
   [clojure.string :as s]
   [oops.core :as oops]))


(defn append-error-message [err msg]
  (let [msg* (oops/oget err "message")
        err (if msg* (oops/oset! err "message" (s/join [msg* msg])) err)]
    err))
