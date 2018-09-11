(ns async.core
  #?(:clj
     (:require
      [async-error.core]
      [clojure.core.async])))


#?(:clj
   (defmacro go-try-loop
     [predicate? ch handler]
     `(async-error.core/go-try
      (loop [result# (async-error.core/<? ~ch)]
        (if (~predicate? result#)       ;; terminating predicate: number?
          result#                  ;; terminating case
          (do (~handler result#) ;; recuring case
              (recur (async-error.core/<? ~ch))))))))
