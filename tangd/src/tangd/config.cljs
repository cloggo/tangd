(ns tangd.config
  (:require [tangd.controller.hello :as hello]))


(def routes
  [[ :get "/hello/:name" hello/respond ]
   [ :head "/hello/:name" hello/respond ]])

