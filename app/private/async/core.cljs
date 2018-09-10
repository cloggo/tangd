(ns async.core
  #_(:require-macros [cljs.core.async.macros :as m-async :refer [alt!]])
  (:require
   #_[cljs.core.async.impl.channels :refer [ManyToManyChannel]]
   [func.core :as func]
   [clojure.core.async :as async :refer [alts!]]) )

(def put! async/put!)

(defn promise [f]
  (let [resolve-channel (async/chan)
        error-channel (async/chan)]
    (f resolve-channel error-channel)))


(defn go** [f]
  (fn [c] (if (vector? c) (f c) (async/take! c f) )))


(defn go*
  ([f e] (go** (fn [[resolve reject]]
                 (async/go
                   (async/alt!
                     [resolve] ([result] (f result))
                     [reject] ([error] (e error)))))))
  ([f] (go** (fn [[resolve reject]]
               (async/go
                 (async/alt!
                   [resolve] ([result] (f result))
                   [reject] [resolve reject]))))))


(defn map* [f v-ch]
  (let [ [v-result v-error] (func/zip-vector v-ch)
        error (async/merge v-error)
        result (async/map f v-result)]
    [result error]))


(defn any* [f v-ch]
  (let [ [v-result v-error] (func/zip-vector v-ch)
        error (async/merge v-error)
        result (async/merge v-result)]
    [result error]))
