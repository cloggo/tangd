(ns interop.core
  (:require
   [goog.string :as gstring]
   [goog.string.format]
   [oops.core :as oops]))


(defn bind [obj k]
  (.bind (oops/oget+ obj k) obj))

(defn log [& args]
  (oops/oapply js/console :log (into-array args)))

(defn log-error [& args]
  (oops/oapply js/console :error (into-array args)))

(defn logf [& args]
  (.log js/console (apply gstring/format args)))

(defn logf-error [& args]
  (.error js/console (apply gstring/format args)))

(defn constructor-name? [o name]
  (when (= (oops/oget o "constructor.name") name) o))
