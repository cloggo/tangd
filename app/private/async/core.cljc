(ns async.core
  #?(:clj
     (:require
      [async-error.core]
      [cljs.core.async]
      [clojure.core.async])))

#?(:clj
   (defn cljs-env?
     "Take the &env from a macro, and tell whether we are expanding into cljs."
     [env]
     (boolean (:ns env))))



#?(:clj
   (defmacro when-cljs
     "Return then if we are generating cljs code and else for Clojure code.
      https://groups.google.com/d/msg/clojurescript/iBY5HaQda4A/w1lAQi9_AwsJ"
     [then]
     (when (cljs-env? &env) then)))


#?(:clj
   (defmacro <? [& args] `(async-error.core/<? ~@args)))

#?(:clj
   (defmacro chan [& args] `(when-cljs (cljs.core.async/chan ~@args))))

#?(:clj
   (defmacro <! [& args] `(when-cljs (cljs.core.async/<! ~@args))))

#?(:clj
   (defmacro >! [& args] `(when-cljs (cljs.core.async/>! ~@args))))

#?(:clj
   (defmacro go-try [& args] `(async-error.core/go-try ~@args)))

#?(:clj
   (defmacro put! [& args] `(when-cljs (cljs.core.async/put! ~@args))))

#?(:clj
   (defmacro take! [& args] `(when-cljs (cljs.core.async/take! ~@args))))

#?(:clj
   (defmacro map* [& args] `(when-cljs (cljs.core.async/map ~@args))))

#?(:clj
   (defmacro go [& args] `(when-cljs (cljs.core.async/go ~@args))))


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
