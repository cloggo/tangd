(ns func.core)

;; create execution stack (chaining callback)
(defn foldr
  ([f coll] (foldr f (peek coll) coll)) ;; reduce from right based on foldr
  ([f v coll] ;; actually doing foldr
   (defn foldr* [result coll]
     (let [coll (pop coll)
           val (peek coll)]
       #_(println cmd-wrap)
       (if (empty? coll)
         result
         (recur (f result val) coll))))
   (foldr* v coll)))


(defn zip-vector [v]
  (reduce-kv
   (fn [arr _ val] [(conj (first arr) (first val)) (conj (second arr) (second val))])
   [[][]]
   v))
