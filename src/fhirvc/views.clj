(ns fhirvc.views
  (:require [fhirvc.core :as fc]
            [hiccup.core :as hc]
            [hiccup.page :as page]
            [config.core :refer [env]]))

(defn layout [title & cnt]
  (hc/html
   [:html
    [:head
     [:title title]
     (page/include-css "/css/foundation.min.css" "/css/styles.css" "/css/jquery.json-viewer.css")]
    [:body cnt]]))
                             

(defn index []
  (layout "FHIR version comparator"
          [:div {:class "row"}
           [:h2 "Choose versions to compare"]]
          [:div {:class "row"}
           [:div {:class "large-10 medium-10"}
            [:form
             [:div {:class "row"}
              (for [param-name ["first_doc_name", "sec_doc_name"]]
                [:div {:class "large-5 medium-5 columns"}
                 [:select {:name param-name}
                  (for [name (fc/get-version-names)]
                    [:option {:value name} name])]])]
             [:div {:class "row"}
              [:div {:class "large-offset-5 medium-offset-5 large-2 medium-2 columns"}
               [:input {:type "submit" :value "Compare" :class "expanded button"}]]]]]]
          [:div {:class "row"}
           [:h2 "results"]
           [:ul.root]]
  (page/include-js "js/jquery.js" "js/jquery.json-viewer.js" "js/script.js")))



