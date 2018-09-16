(ns app.controller.rec
  (:require
   [async.core :as async :refer-macros [<?* <?_ <? go-try go  <!]]
   [app.service.adv :as adv]
   [sqlite.core :as sqlite]
   [async.restify.core :as restify]
   [jose.core :as jose]
   [oops.core :as oops]
   [app.service.rec :as rec]))

(defn verify-request [jwk]
  (if (jose/jwk-prm jwk false "deriveKey")
    (let [kty (jose/json-get jwk "kty")
          alg (jose/json-get jwk "alg")]
      (if-not (= kty "EC")
        {:status :BAD_REQUEST :error "invalid kty"}
        (when-not (= alg "ECMR") {:status :BAD_REQUEST :error "invalid algorithm"})))
    {:status :FORBIDDEN :error "Not a deriveKey jwk"}))


(defn verify-local [jwk]
  (if (jose/jwk-prm jwk true "deriveKey")
    (let [d (jose/json-get jwk "d")
          alg (jose/json-get jwk "alg")]
      (if-not d
        {:status :BAD_REQUEST :error "invalid d"}
        (when-not (= alg "ECMR") {:status :FORBIDDEN :error "invalid algorithm"})))
    {:status :FORBIDDEN :error "Not a deriveKey jwk"}))


(def result {:headers #js {:content-type "application/jwk+json"}
             :status :OK})


(defn rec-jwk-exc [req]
  (go-try
   (let [req-jwk (oops/oget req :body)]
     (or (verify-request req-jwk)
         (let [thp (oops/oget req :params :kid)
               ch (rec/get-jwk-from-thp (sqlite/on-db) thp)
               result (<? ch)
               jwk (.-jwk result)
               jwk (jose/json-loads jwk)]
           (or (verify-local jwk)
               (let [rep (jose/jwk-exc jwk req-jwk)
                     alg (jose/json-loads "{\"alg\": \"ECMR\"}")
                     key-op (jose/json-loads "{\"key_ops\": [\"deriveKey\"]}")]
                 (jose/json-object-update rep alg)
                 (jose/json-object-update rep key-op)
                 (assoc result :payload rep))))))))


(restify/reg-http-request-handler
 :rec
 (fn [[req]]
   (go (->> (rec-jwk-exc req)
            (<!) (restify/check-error-result)
            (restify/http-response :rec)))))
