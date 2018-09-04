(ns func.core)

;; create execution stack (chaining callback)
(defn foldr
  ([f coll] (foldr f (peek coll) coll))
  ([f v coll]
   (defn foldr* [result coll]
     (let [coll (pop coll)
           val (peek coll)]
       #_(println cmd-wrap)
       (if (empty? coll)
         result
         (recur (f result val) coll))))
   (foldr* v coll)))

