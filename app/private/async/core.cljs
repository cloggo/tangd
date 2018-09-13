(ns async.core
  (:require-macros [async.core :as async]
                   [async-error.core]
                   [clojure.core.async])
  (:require
   [async-error.core]
   [cljs.core.async]
   [clojure.string :as s]
   [oops.core :as oops]))

(def *routes* (atom {}))

(defn reg-route [route-key ch pub]
  (swap! *routes* assoc route-key [ch pub]))

(defn get-route-ch [route-key]
  (get-in @*routes* [route-key 0]))

(defn get-route-pub [route-key]
  (get-in @*routes* [route-key 1]))


(defn append-error-message [err msg]
  (let [msg* (oops/oget err "message")
        err (if msg* (oops/oset! err "message" (s/join [msg* msg])) err)]
    err))


(defn push [route-key handler-key data]
  (async/put! (get-route-ch route-key) {route-key handler-key :data data}))

