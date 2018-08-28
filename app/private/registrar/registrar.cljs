(ns registrar.registrar)

(def atom- (atom {}))

(defn set-data! [path data]
  (swap! atom- assoc-in path data))

(defn wrap-callback [f [data res-spec dispatch-data]]
  [(f data) res-spec dispatch-data])


(defn wrap-filter [f [data res-spec dispatch-data]]
  (let [r (f data res-spec dispatch-data)
        {:keys [data res-spec dispatch-data]
         :or {data data res-spec res-spec dispatch-data dispatch-data}} r]
    [data res-spec dispatch-data]))


(defn compose-fv [fv]
  (if (empty? fv) identity (->> fv (rseq) (apply comp))))


(defn reg-fx [id handler & filters]
  (let [[fv1 fv2 fv3] (mapv (fn [v] (mapv #(partial wrap-filter %) v)) filters)
        [pre-callback post-callback post-dispatch] (mapv compose-fv [fv1 fv2 fv3])
        handler (partial wrap-filter handler)]
    (swap! atom- assoc-in [:fx id] [handler pre-callback post-callback post-dispatch])))


(defn reg-evt [id fx-id callback & [init-data init-spec]]
  (swap! atom- assoc-in [:evt id] [fx-id (partial wrap-callback callback) init-data init-spec]))


(defn dispatch! [evt-id & [dispatch-data]]
  (let [[fx-id callback init-data init-spec] (get-in @atom- [:evt evt-id])
        [fx-handler pre-callback post-callback post-dispatch] (get-in @atom- [:fx fx-id])]
    (-> [init-data init-spec dispatch-data]
        (pre-callback)
        (callback)
        (post-callback)
        (fx-handler)
        (post-dispatch))))
