(ns app.config)

(def app-name "tangd")

(def port 8080)

(def response-headers #js {:content-type "json"})

(def server-options
  #js {:ignoreTrailingSlash true
       :name app-name})

(def response-defaults
  {:headers response-headers
   :status :OK
   :next? false})
