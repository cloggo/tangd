(ns func.core)

;; create execution stack (chaining callback)
(defn foldr [f]
  (defn foldr*
    ([coll]
     (foldr* (f (peek coll)) coll))
    ([result coll]
     (let [coll (pop coll)
           val (peek coll)]
       #_(println cmd-wrap)
       (if (empty? coll)
         result
         (recur (f result val) coll)))))
  foldr*)

