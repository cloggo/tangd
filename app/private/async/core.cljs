(ns async.core
  (:require-macros [async.core :as async]
                   [async-error.core]
                   [clojure.core.async])
  (:require
   [async-error.core]
   [cljs.core.async]
   [clojure.string :as s]
   [oops.core :as oops]))

(def route-chan (async/chan))

(def *classes* (atom {}))


(defn append-error-message [err msg]
  (let [msg* (oops/oget err "message")
        err (if msg* (oops/oset! err "message" (s/join [msg* msg])) err)]
    err))

(defn reg-handler-class [name]
  (swap! *classes* assoc name (async/pub route-chan name)))

(defn subscribe [handler-class route-name ch]
  (async/sub (handler-class @*classes*) route-name ch))

(defn unsubscribe [handler-class route-name  ch]
  (async/unsub (handler-class @*classes*) route-name  ch))

(defn push [handler-class route-name data]
  (async/put! route-chan {handler-class route-name :data data}))

