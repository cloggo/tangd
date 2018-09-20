(ns app.controller.keys
  (:require
   [async-restify.core :as restify]
   [sqlite.core :as sqlite]
   [oops.core :as oops]
   [async-sqlite.core :as sqlite* :refer-macros [transaction]]
   [cljs-async.core :as async :refer-macros [<?* <?_ <? go-try go  <!]]
   [app.service.keys :as keys]
   [clojure.string :as str]))


(defn insert-thp-jwk [db jwk]
  (fn [result]
    (let [jwk-id (oops/oget result :lastID)]
      (go-try
       (->> ((keys/insert-thp db jwk) result)
            (mapv #(go-try ((keys/insert-thp-jwk db jwk-id) (<? %)))))))))


(defn rotate-keys*
  ([db init-vals]
   (let [[es512 ecmr payload jws] init-vals]
     (rotate-keys* db init-vals
                   (fn []
                     (keys/cache-defaults jws)
                     {:status :CREATED}))))
  ([db init-vals after-func]
   (let [[es512 ecmr payload jws] init-vals]
     (sqlite*/transaction
      db [after-func]
      (go-try
       (-> (keys/insert-jwk db ecmr)
           (<?) ((insert-thp-jwk db ecmr))
           (<?_ (keys/insert-jwk db es512))
           (<?) ((insert-thp-jwk db es512))
           (<?_ (keys/drop-jws-table db))
           (<?_ (keys/create-jws-table db))
           (<?_ (keys/create-jws-jwk-index db))
           (<?_ (keys/select-all-jwk db))
           (<?* number? (keys/insert-jws db payload es512))))))))


(defn rotate-keys
  ([db after-func] (rotate-keys* db (keys/rotate-keys) after-func))
  ([db] (rotate-keys* db (keys/rotate-keys))))


(restify/reg-http-request-handler
 :keys
 (fn [[req]]
   (let [remote-ip (or (oops/oget req :headers "x-forward-for")
                       (oops/oget req :connection :remoteAddress))
         remote-ip (str/replace-first remote-ip #"::fff:" "")]
     (if (some #(= remote-ip %) (keys/ip-whitelist))
         (go
           (->> (rotate-keys (sqlite/on-db))
                (<!) (restify/check-error-result)
                (restify/http-response :keys)))
         (restify/http-response
          :keys
          {:error (str/join ["remote ip " remote-ip " is not allowed."])
           :status :FORBIDDEN})))))

;; (def handler (restify/handle-route restify-route-event))
