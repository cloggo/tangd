(ns app.controller.rec
  (:require
   [cljs-async.core :as async :refer-macros [<?* <?_ <? go-try go  <!]]
   [app.service.adv :as adv]
   [sqlite.core :as sqlite]
   [async-restify.core :as restify]
   [jose.core :as jose]
   [oops.core :as oops]
   [app.service.rec :as rec]))

(defn verify-request [jwk]
  (if (and jwk (jose/jwk-prm jwk false "deriveKey"))
    (let [kty (jose/json-value-get (jose/json-get jwk "kty"))
          alg (jose/json-value-get (jose/json-get jwk "alg"))]
      (if (= kty "EC")
        (when-not (= alg "ECMR") {:status :BAD_REQUEST :error "invalid algorithm"})
        {:status :BAD_REQUEST :error "invalid kty"}))
    {:status :FORBIDDEN :error "Not a deriveKey jwk"}))


(defn verify-local [jwk]
  (if (and jwk (jose/jwk-prm jwk true "deriveKey"))
    (let [d (jose/json-value-get (jose/json-get jwk "d"))
          alg (jose/json-value-get (jose/json-get jwk "alg"))]
      #_(println d alg)
      (if d
        (when-not (= alg "ECMR") {:status :FORBIDDEN :error "invalid algorithm"})
        {:status :BAD_REQUEST :error "invalid d"}))
    {:status :FORBIDDEN :error "Not a deriveKey jwk"}))


(def respond-templ {:headers #js {:content-type "application/jwk+json"}
             :status :OK})



(defn rec-jwk-exc [req]
  (go-try
   (if (restify/acceptable-content? req "application/jwk+json")
     (let [req-jwk (oops/oget req :body)]
       #_(println "req: " (jose/json-dumps req-jwk))
       (or (verify-request req-jwk)
           (let [thp (oops/oget req :params :kid)
                 ch (rec/get-jwk-from-thp (sqlite/on-db) thp)
                 result (<? ch)
                 jwk (oops/oget result :jwk)
                 jwk (and jwk (jose/json-loads jwk))]
             (or (verify-local jwk)
                 #_(println "local: " (jose/json-dumps jwk))
                 (let [rep (jose/jwk-exc jwk req-jwk)
                       alg (jose/json-loads "{\"alg\": \"ECMR\"}")
                       key-op (jose/json-loads "{\"key_ops\": [\"deriveKey\"]}")]
                   (jose/json-object-update rep alg)
                   (jose/json-object-update rep key-op)
                   #_(println "exc: " (jose/json-dumps rep))
                   (assoc respond-templ :payload rep))))))
     {:status :NOT_ACCEPTABLE :error "content is not application/jwk+json"})))


(restify/reg-http-request-handler
 :rec
 (fn [[req]]
   (go (->> (rec-jwk-exc req)
            (<!) (restify/check-error-result)
            (restify/http-response :rec)))))
