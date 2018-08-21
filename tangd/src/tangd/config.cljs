(ns tangd.config)

(def port 8080)

(def res-headers #js {:content-type "json"})

(def server-options
  #js {:ignoreTrailingSlash true
       :name "tangd"})

