(ns async.sqlite.core
  #_(:require-macros [cljs.core.async.macros :as m-async :refer [alt!]])
  (:require
   #_[cljs.core.async.impl.channels :refer [ManyToManyChannel]]
   [async.core :as async]
   [restify.const* :as const]
   [interop.core :as interop]
   [sqlite.core :as q]))


(defn on-cmd** [ch db cmd stmt & params]
  ((apply q/on-cmd db cmd stmt params)
   (fn [result] (async/put! ch result))
   (fn [err] (async/put! ch err))))


(defn on-cmd [db cmd stmt & params]
  (let [ch (async/chan)]
    (apply on-cmd** ch db cmd stmt params)
    ch))


(defn on-cmd* [ch-buf-size]
  (let [ch (async/chan ch-buf-size)]
    (fn [db cmd stmt & params]
      (apply on-cmd** ch db cmd stmt params)
      ch)))


(defn on-cmd-stmt [stmt cmd & params]
  (let [ch (async/chan)
        stmt-cmd (interop/bind stmt cmd)
        stmt-cmd (if (empty? params) stmt-cmd (apply partial stmt-cmd params))]
    (stmt-cmd (fn [err]
                (if err (async/put! ch err)
                    (this-as result (async/put! ch result)))))
    ch))


(defn begin-transaction [db]
  (on-cmd db :run "BEGIN TRANSACTION;"))

(defn commit-transaction [db]
  (on-cmd db :run "COMMIT;"))

(defn rollback-transaction [db]
  (on-cmd db :run "ROLLBACK;"))

