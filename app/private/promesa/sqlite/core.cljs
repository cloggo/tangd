(ns promesa.sqlite.core
  (:require
   [interop.core :as interop]
   [sqlite.core :as q]
   [promesa.core :as p]))

(defn on-cmd [db cmd stmt & params]
  (p/promise
   (fn [resolve reject]
     ((apply q/on-cmd db cmd stmt params)
      (fn [result] (resolve result))
      (fn [err] (reject err))))))


(defn on-cmd-stmt
  [stmt cmd & params]
  (let [stmt-cmd (interop/bind stmt cmd)
        stmt-cmd (if (empty? params) stmt-cmd (apply partial stmt-cmd params))]
    (p/promise
     (fn [resolve reject]
       (stmt-cmd
        (fn [err]
          (if err (reject err)
              (this-as result (resolve result)))))))))
