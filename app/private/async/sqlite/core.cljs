(ns async.sqlite.core
  #_(:require-macros [cljs.core.async.macros :as m-async :refer [alt!]])
  (:require
   [interop.core :as interop]
   [sqlite.core :as q]
   [clojure.core.async :as async :refer [alts!]]))


(defn promise [f]
  (let [resolve-channel (async/chan)
        error-channel (async/chan)]
    (f resolve-channel error-channel)))


(defn on-cmd [db cmd stmt & params]
  (promise
   (fn [resolve reject]
     ((apply q/on-cmd db cmd stmt params)
      (fn [result] (async/put! resolve result))
      (fn [err] (async/put! reject err)))
     [resolve reject])))


(defn on-cmd-stmt
  [stmt cmd & params]
  (let [stmt-cmd (interop/bind stmt cmd)
        stmt-cmd (if (empty? params) stmt-cmd (apply partial stmt-cmd params))]
    (promise
     (fn [resolve reject]
       (stmt-cmd
        (fn [err]
          (if err (async/put! reject err)
              (this-as result (async/put! resolve result)))))
       [resolve reject]))))


(defn then
  ([f e] (fn [[resolve reject]]
           (async/go
             (async/alt!
               [resolve] ([result] (f result))
               [reject] ([error] (e error))))))
  ([f] (fn [[resolve reject]]
         (async/go
           (async/alt!
             [resolve] ([result] (f result))
             [reject] [resolve reject])))))
