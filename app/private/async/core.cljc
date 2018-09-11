(ns async.core
  #?(:clj
     (:require
      [async-error.core]
      [clojure.core.async])))


#?(:clj
   (defmacro async-stream
     ([ch handler]
      `(let [ch# ~ch]
         (async-error.core/go-try
          (while true (~handler (async-error.core/<? ch#))))))
     ([ch predicate? handler]
      `(let [ch# ~ch]
         (async-error.core/go-try
          (loop [result# (async-error.core/<? ch#)]
            (if (~predicate? result#)       ;; terminating predicate: number?
              result#                  ;; terminating case
              (do (~handler result#) ;; recuring case
                  (recur (async-error.core/<? ch#))))))))))
