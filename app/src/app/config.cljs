(ns app.config
  (:require [app.lib.transit-formatter :as transit-formatter]))

(def app-name "tangd")

(def port 8080)

(def response-headers #js {:content-type "application/transit+json"})

(def server-options
  #js {:ignoreTrailingSlash true
       :name app-name
       :formatters (js-obj "application/transit+json"
                           transit-formatter/transit-format)})

(def response-defaults
  {:headers response-headers
   :status :OK
   :next? false})
