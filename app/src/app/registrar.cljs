(ns app.registrar
  (:require [app.lib.interop :as interop]))

(def atom- (atom {}))

(defn reg-fx [id handler]
  (swap! atom- assoc-in [:fx id] handler))

(defn reg-evt [id fx-id callback]
  (swap! atom- assoc-in [:evt id] [fx-id callback]))

(defn dispatch! [evt-id data]
  (let [[fx-id evt-callback] (get-in @atom- [:evt evt-id])
        fx-handler (get-in @atom- [:fx fx-id])]
    (fx-handler evt-callback data)))
