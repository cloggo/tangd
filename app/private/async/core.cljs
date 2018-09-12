(ns async.core
  (:require-macros [async.core]
                   [async-error.core]
                   [clojure.core.async])
  (:require
   [async-error.core]
   [cljs.core.async]
   [clojure.string :as s]
   [oops.core :as oops]))


(defn append-error-message [err msg]
  (let [msg* (oops/oget err "message")
        err (if msg* (oops/oset! err "message" (s/join [msg* msg])) err)]
    err))
