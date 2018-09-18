(ns app.controller.adv
  (:require
   [async.async.core :as async :refer-macros [<?* <?_ <? go-try go  <!]]
   [app.service.adv :as adv]
   [sqlite.core :as sqlite]
   [async.restify.core :as restify]
   [oops.core :as oops]
   [app.service.keys :as keys]))


(defn create-jws-spec [jws]
  (def result {:send-mode :sendRaw
               :headers #js {:content-type "application/jose+json"}
               :status :OK})
  (if jws
    (assoc result :payload jws :status :OK)
    (assoc result :error "invalid thumbprint" :status :NOT_FOUND)))


(restify/reg-http-request-handler
 :adv
 (fn [context]
   (restify/http-response :adv
                          (create-jws-spec (keys/default-jws)))))


(restify/reg-http-request-handler
 :adv-kid
 (fn [[req]]

   #_((sqlite/on-cmd (sqlite/on-db) :all "select * from jws;") (fn [r] (println r)))

   (go (->> (go-try
             (->> (oops/oget req :params :kid)
                  (adv/get-jws-from-thp (sqlite/on-db))
                  (<?) (#(oops/oget % :jws))
                  (create-jws-spec)))
            (<!) (restify/check-error-result)
            (restify/http-response :adv-kid)))))

