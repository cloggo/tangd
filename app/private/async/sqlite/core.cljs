(ns async.sqlite.core
  #_(:require-macros [cljs.core.async.macros :as m-async :refer [alt!]])
  (:require
   #_[cljs.core.async.impl.channels :refer [ManyToManyChannel]]
   [clojure.core.async :as async :refer [put!]]
   [restify.const* :as const]
   [func.core :as func]
   [interop.core :as interop]
   [sqlite.core :as q]))


(defn create-db-error [err]
  (interop/create-error (:METHOD_FAILURE const/http-status) err))


(defn on-cmd [db cmd stmt & params]
  (let [ch (async/chan)]
     ((apply q/on-cmd db cmd stmt params)
      (fn [result] (async/put! ch result))
      (fn [err] (throw (create-db-error err))))
     ch))


(defn on-cmd-stmt [stmt cmd & params]
  (let [ch (async/chan)
        stmt-cmd (interop/bind stmt cmd)
        stmt-cmd (if (empty? params) stmt-cmd (apply partial stmt-cmd params))]
    (stmt-cmd (fn [err]
                (if err (throw (create-db-error err))
                    (this-as result (async/put! ch result)))))
    ch))

