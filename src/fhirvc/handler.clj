(ns fhirvc.handler
  (:require [fhirvc.core :as fc]
            [fhirvc.views :as views]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [org.httpkit.server :as ohs]
            [hiccup.core :as hc]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]))

(defn index [req]
  {:body (views/index)
   :status 200
   :headers {"content-type" "text/html; charset=utf-8"}})

(defn fhir-versions-diff [req]
  (let [{version-a :first_doc_name version-b :sec_doc_name} (:params req)
        json-diff (fc/versions-diff version-a version-b)]
    (println json-diff)
    {:body json-diff
     :headers {"content-type" "application/json"}
     :status 200}))

(defroutes app-routes
  (GET "/" [] #'index)
  (GET "/compare" [] #'fhir-versions-diff)
  (route/resources "/")
  (route/not-found "Page not Found"))

(def app
  (wrap-defaults app-routes api-defaults))
      

(def stop (ohs/run-server #'app {:port 8080}))
