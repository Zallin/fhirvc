(ns fhirvc.handler
  (:require [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.util.response :refer [redirect]]
            [ring.middleware.not-modified :refer :all]))

(defn redirect-handler [req]
  (redirect "/index.html"))

; load path from config
(def app
  (-> redirect-handler
      (wrap-resource "site")
      wrap-content-type))
