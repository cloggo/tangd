(ns tangd.lib.interop
  (:require
   [goog.string :as gstring]
   [goog.string.format]
   [oops.core :as oops]))


(defn ometh [obj k]
  (.bind (oops/oget+ obj k) obj))

;;;; println is enabled by (enable-util-print!)
(defn log [& args]
  (.log js/console (apply gstring/format args)))

(defn log-error [& args]
  (.error js/console (apply gstring/format args)))

