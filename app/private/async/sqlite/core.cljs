(ns async.sqlite.core
  #_(:require-macros [cljs.core.async.macros :as m-async :refer [alt!]])
  (:require
   #_[cljs.core.async.impl.channels :refer [ManyToManyChannel]]
   [async.core :as async]
   [func.core :as func]
   [interop.core :as interop]
   [sqlite.core :as q]))


(defn on-cmd [db cmd stmt & params]
  (async/promise
   (fn [resolve reject]
     ((apply q/on-cmd db cmd stmt params)
      (fn [result] (async/put! resolve result))
      (fn [err] (async/put! reject err)))
     [resolve reject])))


(defn on-cmd-stmt
  [stmt cmd & params]
  (let [stmt-cmd (interop/bind stmt cmd)
        stmt-cmd (if (empty? params) stmt-cmd (apply partial stmt-cmd params))]
    (async/promise
     (fn [resolve reject]
       (stmt-cmd
        (fn [err]
          (if err (async/put! reject err)
              (this-as result (async/put! resolve result)))))
       [resolve reject]))))

