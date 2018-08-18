(ns tangd.core
  (:require [cljs.nodejs :as nodejs]))

(nodejs/enable-util-print!)

(defn -main []
  (println "Hello world 3!"))

(set! *main-cli-fn* -main)
