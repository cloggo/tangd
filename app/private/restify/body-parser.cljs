(ns restify.body-parser
  (:require [restify]
            [restify-errors :as errors]
            [restify.transit-parser :as transit-parser]
            ;; [app.lib.fast-json-parser :as json-parser]
            [oops.core :as oops]))

(def ^{:dynamic true :private true} *parsers*
  {"application/x-www-form-urlencoded" #(aget (oops/ocall restify "plugins.urlEncodedBodyParser" %) 0)
   "multipart/form-data" #(oops/ocall restify "plugins.multipartBodyParser" %)
   "application/json" #(aget (oops/ocall restify "plugins.jsonBodyParser" %) 0)
   "application/transit+json" (fn [_] transit-parser/transit-parser)} )

(defn merge-parser! [parser]
  (set! *parsers* (merge parser *parsers*)))

(defn parse-body- [parsers]
  (fn [req res next]
    (let [method (oops/oget req :method)
          is-chunked (oops/ocall req :isChunked)
          length (oops/ocall req :contentLength)]
      (cond
        (oops/oget req "?_parsedBody") (next)
        (= method "HEAD") (next)
        (= method "GET") (next)
        (and (= length 0) (not is-chunked)) (next)
        :else
        (let [content-type (oops/ocall req :contentType)
              content-parser (get parsers content-type
                                  (when (re-matches #"(?i)application/.*\+json" content-type)
                                    (get parsers "application/json")))]
          (oops/oset! req "!_parsedBody" true)
          (if content-parser
            (content-parser req res next)
            (next (errors/UnsupportedMediaTypeError. content-type))))))))

(defn body-parser
  ([] body-parser #js {})
  ([opts] (let [_ (oops/oset! opts "!bodyReader" true)
                body-reader (oops/ocall restify "plugins.bodyReader" opts)
                parsers (reduce-kv #(assoc %1 %2 (%3 opts)) {} *parsers*)]
            #js [body-reader (parse-body- parsers)])))

