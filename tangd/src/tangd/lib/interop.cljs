(ns tangd.lib.interop
  (:require
   [goog.string :as gstring]
   [goog.string.format]
   [oops.core :as oops]))


;; Javascript: context
;; function is an object implicitly have method call and apply
;; f(a) == f.call(global, a); global = global for Node.JS, global = window for browser
;; o.f(a) == f.call(o, a)
;; new f(a) == var unnamed_object, f.call(unamed_object, a)
;; f.apply(context, arr) == f.call(context, arr[0], arr[1], ..., arr[N-1]), N = length of arr
(defn ometh [obj k]
  (.bind (oops/oget+ obj k) obj))

;;;; println is enabled by (enable-util-print!)
(defn log [& args]
  (.log js/console (apply gstring/format args)))

(defn log-error [& args]
  (.error js/console (apply gstring/format args)))

