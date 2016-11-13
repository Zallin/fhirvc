(ns fhirvc.handler
  (:require [config.core :refer [env]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.util.response :refer [redirect]]
            [me.raynes.fs :refer [base-name]]))

(defn redirect-handler [req]
  (if (= (:uri req) "/")
    (redirect "/index.html")
    {:body "Not found"
     :status 404}))

(defn resource-folder-path []
  (base-name (:output-folder env)))

(def app
  (-> redirect-handler
      (wrap-resource (resource-folder-path))
      wrap-content-type))
