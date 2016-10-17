(ns fhirvc.views
  (:require [hiccup.core :as hc]
            [hiccup.page :as page]
            [clojure.string :refer [replace lower-case]]
            [fhirvc.adt :refer :all]
            [json-html.core :refer :all]))

(def prefix "zallin.github.io/fhirvc")

(defn layout [title & cnt]
  (hc/html
   [:html
    [:head
     [:title title]
     (page/include-css "zallin.github.io/fhirvc/css/foundation.min.css" "zallin.github.io/fhirvc/css/styles.css")]
    [:body
     [:div.top-bar
      [:div.top-bar-left
       [:ul.dropdown.menu
        [:li.menu-text "FHIR version comparator"]]]]
     [:div cnt]]]))

(defn index [comp-seq]
  (layout "FHIRvc | Choose versions to compare"
          [:div.row
           [:h3 "Choose versions to compare"]]          
          [:div.row 
           [:div.large-10.medium-10 
            [:form
             [:div.row
                [:div.large-5.medium-5.columns 
                 [:select {:name "versions"}
                  (for [comp comp-seq]
                    (let [[a b] (fhir-names comp)]
                      [:option {:value (comp-ref comp)} (str a " to " b)]))]]]                     
             [:div.row
              [:div.large-2.medium-2.columns 
               [:input.expanded.button {:type "submit" :value "Compare"}]]]]]]
  (page/include-js "zallin.github.io/fhirvc/js/jquery.js" "zallin.github.io/fhirvc/js/script.js")))

(defn section [header defs comp]
  (if (empty? defs)
    [:div.row
     [:h4 (str "No " (lower-case header))]]
    [:div.row
     [:h4 header]
     [:ul
      (for [def defs]
        [:li [:a {:href (def-ref def comp)} (def-name def)]])]]))

(defn version-comparison [comparison]
  (let [diff (fhir-diff comparison)]
    (layout "FHIRvc | Comparison summary"
            [:div.row
             (let [[a b] (fhir-names comparison)]
               [:h3 (str a " compared to " b)])]
            (section "Added definitions" (added diff) comparison)
            (section "Removed definitions" (removed diff) comparison)
            (section "Changed defitinions" (changed diff) comparison)
            (section "Unchanged definitions" (unchanged diff) comparison))))

(defn definition [def]
  (layout "FHIRvc | Definition"
          [:div.row
           [:h3 (str "Resource type: " (def-type def))]
           [:h3 (str "Resource name: " (def-name def))]]
          [:div.row
           (edn->html def)]))                                                      
