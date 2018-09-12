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
   (defmacro reduce* [& args] `(when-cljs (cljs.core.async/reduce ~@args))))

#?(:clj
   (defmacro merge* [& args] `(when-cljs (cljs.core.async/merge ~@args))))

#?(:clj
   (defmacro alt! [& args] `(when-cljs (cljs.core.async/alt! ~@args))))

#?(:clj
   (defmacro alts! [& args] `(when-cljs (cljs.core.async/alts! ~@args))))

#?(:clj
   (defmacro amix [& args] `(when-cljs (cljs.core.async/amix ~@args))))

#?(:clj
   (defmacro mix [& args] `(when-cljs (cljs.core.async/mix ~@args))))

#?(:clj
   (defmacro into* [& args] `(when-cljs (cljs.core.async/into* ~@args))))

#?(:clj
   (defmacro take* [& args] `(when-cljs (cljs.core.async/take* ~@args))))

#?(:clj
   (defmacro go-loop [& args] `(when-cljs (cljs.core.async/go-loop ~@args))))

#?(:clj
   (defmacro sub [& args] `(when-cljs (cljs.core.async/sub ~@args))))

#?(:clj
   (defmacro pub [& args] `(when-cljs (cljs.core.async/pub ~@args))))

#?(:clj
   (defmacro pipeline [& args] `(when-cljs (cljs.core.async/pipeline ~@args))))

#?(:clj
   (defmacro pipeline-async [& args] `(when-cljs (cljs.core.async/pipeline-async ~@args))))

#?(:clj
   (defmacro pipeline-blocking [& args] `(when-cljs (cljs.core.async/pipeline-blocking ~@args))))

#?(:clj
   (defmacro promise-chan [& args] `(when-cljs (cljs.core.async/promise-chan ~@args))))

#?(:clj
   (defmacro solo-mode [& args] `(when-cljs (cljs.core.async/solo-mode ~@args))))

#?(:clj
   (defmacro timeout [& args] `(when-cljs (cljs.core.async/timeout ~@args))))

#?(:clj
   (defmacro to-chan [& args] `(when-cljs (cljs.core.async/to-chan ~@args))))

#?(:clj
   (defmacro toggle [& args] `(when-cljs (cljs.core.async/toggle ~@args))))

#?(:clj
   (defmacro split [& args] `(when-cljs (cljs.core.async/split ~@args))))

#?(:clj
   (defmacro transduce* [& args] `(when-cljs (cljs.core.async/transduce ~@args))))

