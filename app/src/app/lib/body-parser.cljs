(ns app.lib.body-parser
  (:require [cognitect.transit :as t]
            [restify]
            [restify-errors :as errors]
            [oops.core :as oops]))

(defn transit-parser [req, res, next]
  (let [r (t/reader :json)
        body (oops/oget req :body)]
    (oops/oset! req "!_body" body)
    (oops/oset! req "!rawBody" body)
    (oops/oset! req "!isTransit" true)
    (oops/oset! req :body (t/read r (oops/ocall body :toString)))
    #_(println "transit: " (oops/oget req :body))
    (next)))

(defn parse-body- [parsers]
  (fn [req res next]
    (let [method (oops/oget req :method)
          is-chunked (oops/ocall req :isChunked)
          length (oops/ocall req :contentLength)
          content-type (oops/ocall req :contentType)]
      (cond
        (oops/oget req "?_parsedBody") (next)
        (= method "HEAD") (next)
        (= method "GET") (next)
        (and (= length 0) (not is-chunked)) (next)
        :else
        (let [_ (oops/oset! req "!_parsedBody" true)
              content-parser
              (case content-type
                "application/json" (:transit-parser parsers)
                "application/transit-json" (:transit-parser parsers)
                "application/transit-msgpack" (:transit-parser parsers)
                "application/x-www-form-urlencoded" (aget (:form-parser parsers) 0)
                "multipart/form-data" (:multipart-parser parsers)
                (when (re-matches #"(?i)application/.*(json|msgpack)" content-type)
                      (:transit-parser parsers)))]
          (if (nil? content-parser)
            (next (errors/UnsupportedMediaTypeError. content-type))
            (content-parser req res next)))))))

(defn body-parser [& [options]]
  (let [opts (or options #js {})
        _ (oops/oset! opts "!bodyReader" true)
        body-reader (oops/ocall restify "plugins.bodyReader" opts)
        parsers {:form-parser (oops/ocall restify "plugins.urlEncodedBodyParser" opts)
                 :multipart-parser (oops/ocall restify "plugins.multipartBodyParser" opts)
                 :transit-parser transit-parser}]
    #js [body-reader (parse-body- parsers)]))

