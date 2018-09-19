(ns app.controller.keys
  (:require
   [async-restify.core :as restify]
   [sqlite.core :as sqlite]
   [oops.core :as oops]
   [async-sqlite.core :as sqlite* :refer-macros [transaction]]
   [cljs-async.core :as async :refer-macros [<?* <?_ <? go-try go  <!]]
   [app.service.keys :as keys]))


(defn insert-thp-jwk [db jwk]
  (fn [result]
    (let [jwk-id (oops/oget result :lastID)]
      (go-try
       (->> ((keys/insert-thp db jwk) result)
            (mapv #(go-try ((keys/insert-thp-jwk db jwk-id) (<? %)))))))))


(defn rotate-keys* [db init-vals]
  (let [[es512 ecmr payload jws] init-vals]
    (sqlite*/transaction
     db [(fn [_]
           (keys/cache-defaults jws)
           {:status :CREATED})]
     (go-try
      (-> (keys/insert-jwk db ecmr)
          (<?) ((insert-thp-jwk db ecmr))
          (<?_ (keys/insert-jwk db es512))
          (<?) ((insert-thp-jwk db es512))
          (<?_ (keys/drop-jws-table db))
          (<?_ (keys/create-jws-table db))
          (<?_ (keys/create-jws-jwk-index db))
          (<?_ (keys/select-all-jwk db))
          (<?* number? (keys/insert-jws db payload es512)))))))


(defn rotate-keys [db]
  (let [init-vals (keys/rotate-keys)
        [es512 ecmr payload jws] init-vals]
    (rotate-keys* db init-vals)))


(defn rotate-and-exit [_]
  (async/go
    (let [db (sqlite/on-db)
          _ (<! (rotate-keys db))]
      (do (println "done..." )
          (<! (async/timeout 1000))
          (oops/ocall js/process :exit)))))


(defn init-and-exit [stmts]
  (fn [db-name]
    (sqlite/set-db-name! db-name)
    (go
      (->> (keys/init-db (sqlite/on-db) stmts)
           (<!)
           ((fn [_]
              #_(sqlite/db-close (sqlite/on-db))
              (oops/ocall js/process :exit)))))))


(restify/reg-http-request-handler
 :keys
 (fn [context]
   (go
     (->> (rotate-keys (sqlite/on-db))
          (<!) (restify/check-error-result)
          (restify/http-response :keys)))))

;; (def handler (restify/handle-route restify-route-event))
