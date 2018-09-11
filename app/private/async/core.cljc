(ns async.core
  #?(:clj
     (:require
      [clojure.string]
      [oops.core]
      [async-error.core]
      [clojure.core.async])))


#?(:clj
   (defmacro <?*
     "repeatedly consumes"
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


#?(:clj
   (defmacro <?_
     "like <? but swallow data by wrapping the body inside a function"
     [ch & body]
     `((fn [_#] ~@body) (async-error.core/<? ~ch))))

#?(:clj
   (defmacro error? [v]
     `(instance? js/Error ~v)))


#?(:clj
   (defmacro not-error? [v]
     `(not (instance? js/Error ~v))))
