(ns app.controller.keys
  (:require
   [clojure.string :as s]
   [jose.core :as jose]
   [app.service.schema :as schema]
   [app.service.keys :as keys]
   [app.coop :as coop]))

(coop/restify-route-event
 :rotate-keys
 (fn [{:keys [db]} [->context]]
   (let [[es512 ecmr payload jws] (keys/rotate-keys)
         sqlite-context ^:->context {:sqlite {:payload payload
                                              :default-jwk-ecmr ecmr
                                              :default-jwk-es512 es512
                                              :default-jws jws}}
         ->context (merge ->context sqlite-context)]
     {:dispatch-n [[:insert-jwk es512 ->context]
                   [:insert-jwk ecmr ->context]
                   [:reset-all-jws ->context]
                   [:rotate-keys-response ->context]]})))



(coop/restify-route-event
 :rotate-keys-response
 (fn [{:keys [db]}]
   {:restify [{:payload {:msg "ok"}}]}))


(coop/restify-route-event
 :reset-all-jws
 (fn [{:keys [db]}]
   {:sqlite-cmd [{:db (get-in db [:sqlite :db])
                  :cmd :run
                  :stmt (s/join [schema/drop-jws-table
                                 schema/create-jws-table
                                 schema/create-jws-jwk-index])
                  :callback :insert-all-jws}]}))


(coop/restify-route-event
 :insert-jwk
 (fn [{:keys [db]} [jwk]]
   {:sqlite-cmd [{:db (get-in db [:sqlite :db])
                  :cmd :run
                  :stmt schema/insert-jwk
                  :params [(jose/json-dumps jwk)]
                  :callback :insert-thp-jws} ^:->context {:sqlite {:jwk jwk}}]}))


(coop/reg-event-fx
 :insert-thp-jws
 (fn [{:keys [db]} [result ->context]]
   (let [jwk-id (.-lastID result)
         {:keys [jwk default-jws]} (:sqlite ->context)]
     #_(println ->context)
     {:dispatch-n [[:insert-thp jwk-id jwk ->context]
                   #_(when (jose/jwk-prm jwk true "sign")
                     [:insert-jws* jwk-id default-jws ->context])]})))

(defn insert-jws** [db jwk-id jws]
  (println "insert-jws**" jwk-id ":" (jose/json-dumps jws))
  {:sqlite-cmd [{:db db
                 :cmd :run
                 :stmt schema/insert-jws
                 :params [jwk-id (jose/json-dumps jws)]}]})


(coop/reg-event-fx
 :insert-jws*
 (fn [{:keys [db]} [jwk-id jws ->context]]
   (insert-jws** (get-in db [:sqlite :db]) jwk-id jws)))


(coop/reg-event-fx
 :insert-all-jws
 (fn [{:keys [db]}]
   {:sqlite-cmd [{:db (get-in db [:sqlite :db])
                  :cmd :each
                  :stmt schema/select-all-jwk
                  :callback :insert-jws}]}))


(coop/reg-event-fx
 :insert-jws
 (fn [{:keys [db]} [result ->context]]
   (let [jwk (jose/json-loads (.-jwk result))]
     #_(println ->context)
     (if (jose/jwk-prm jwk true "sign")
       (let [jwk-id (.-jwk_id result)
             sqlite-db (get-in db [:sqlite :db])
             {:keys [payload default-jwk-es512]} (:sqlite ->context)
             jws (keys/create-jws payload jwk default-jwk-es512)]
         (insert-jws** sqlite-db jwk-id jws)
         )
       {}))))



(coop/reg-event-fx
 :insert-thp
 (fn [{:keys [db]} [jwk-id jwk ->context]]
   #_(println "jwk-id: " jwk-id)
   (let [algs (jose/get-alg (jose/get-alg-kind :JOSE_HOOK_ALG_KIND_HASH))
         thp-vec (mapv #(jose/calc-thumbprint jwk %) algs)]
     #_(println "thp-vec: " thp-vec)
     {:dispatch-n (mapv (fn [thp] [:insert-thp* jwk-id thp ->context]) thp-vec)})))


(coop/reg-event-fx
 :insert-thp*
 (fn [{:keys [db]} [jwk-id thp ->context]]
   {:sqlite-cmd [{:db (get-in db [:sqlite :db])
                  :cmd :run
                  :stmt schema/insert-thp
                  :params [thp]
                  :callback :insert-thp-jwk}]}))


(coop/reg-event-fx
 :insert-thp-jwk
 (fn [{:keys [db]} [result ->context]]
   (let [thp-id (.-lastID result)
         jwk-id (get-in ->context [:sqlite :jwk-id])]
     {:sqlite-cmd [{:db (get-in db [:sqlite :db])
                    :cmd :run
                    :stmt schema/insert-thp-jwk
                    :params [thp-id jwk-id]}]})))
