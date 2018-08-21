(ns app.registrar)

(def atom- (atom {}))

(defn register-fx [id handler]
  (swap! atom- assoc-in [:fx] {id handler}))

(defn register-evt [id fx-id callback]
  (swap! atom- assoc-in [:evt] {id [fx-id callback]}))

(defn dispatch! [evt-id & data]
  (let [[fx-id evt-callback] (get-in @atom- [:evt evt-id])
        fx-handler (get-in @atom- [:fx fx-id])]
    (apply fx-handler evt-callback data)))
