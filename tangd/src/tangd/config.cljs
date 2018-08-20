(ns tangd.config
  (:require [tangd.controller.hello :as hello]))


(def port 8080)


(def routes
  [[ :get "/hello/:name" hello/respond ]
   [ :head "/hello/:name" hello/respond ]])

