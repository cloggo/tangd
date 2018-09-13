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

(def *routes* (atom {}))


(defn append-error-message [err msg]
  (let [msg* (oops/oget err "message")
        err (if msg* (oops/oset! err "message" (s/join [msg* msg])) err)]
    err))

(defn reg-route [name]
  (swap! *routes* assoc name (async/pub route-chan name)))

(defn subscribe [route-name handler-class ch]
  (async/sub (route-name @*routes*) handler-class ch))

(defn unsubscribe [route-name handler-class ch]
  (async/unsub (route-name @*routes*) handler-class ch))

(def reg-handler subscribe)

(defn push [route-name handler-class data]
  (async/put! route-chan {route-name handler-class :data data}))

