(ns app.config)

(def app-name "tangd")

(def port 8080)

(def res-headers #js {:content-type "json"})

(def server-options
  #js {:ignoreTrailingSlash true
       :name app-name})

