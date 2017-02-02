(ns fhirvc.views
  (:require [fhirvc.structure-diff :as struct-diff]
            [fhirvc.filenames :as filenames]
            [hiccup.core :as hc]
            [hiccup.page :as page]
            [clojure.string :refer [lower-case]]
            [json-html.core :refer :all]
            [environ.core :refer [env]]))

(defn prefix [ref]
  (str (:views-path-prefix env) "public/" ref))

(defn layout [title & cnt]
  (hc/html
   [:html
    [:head
     [:title title]
     (page/include-css (prefix "css/foundation.css")
                       (prefix "css/app.css"))]
    [:body
     [:div.top-bar
      [:div.top-bar-left
       [:ul.dropdown.menu
        [:li.menu-text "FHIR version comparator"]]]]
     [:div cnt]
     (page/include-js "//code.jquery.com/jquery-3.1.1.min.js"
                      (prefix "js/vendor/what-input.js")
                      (prefix "js/vendor/foundation.js")
                      (prefix "js/script.js"))]]))

(defn index [comp-seq]
  (layout "FHIRvc | Choose versions to compare"
          [:div.row
           [:h3 "Supported versions"]]
          [:div.row 
           [:div.large-10.medium-10 
            [:form
             [:div.row
              [:ul
               (for [[a b ref] comp-seq]
                 [:li [:a {:href ref} (str a " to " b)]])]]]]]))


(defn without-ref [str-def]
  [:p (get str-def "name")])

(defn column [heading els]
  [:div.large-4.medium-4.columns
   [:h4 heading]
   (into [] (concat [:ul] (map #(vector :li %) els)))])

(defn summary [comparison]  
  (layout "FHIRvc | Comparison summary"
          [:div.row
           [:h3 (str (:old-version comparison) " compared to " (:new-version comparison))]]          
          [:div.row
           (column "Added structure definitions"
                   (map without-ref (struct-diff/added (:structure-difference comparison))))
           (column "Removed structure definitions"
                   (map without-ref (struct-diff/removed (:structure-difference comparison))))
           (column "Changed structure definitions"
                   (map (fn [[name sem-diffs]]
                          [:a {:href (filenames/def-in-comparison comparison [name sem-diffs])} name])
                        (:semantic-difference comparison)))]))

(defmulti semantic-change (fn [diff] (:text diff)))

(defmethod semantic-change "added" [diff]
  [:li [:p.added (:name diff)]])

(defmethod semantic-change "removed" [diff]
  [:li [:p.removed (:name diff)]])

(defmethod semantic-change :default [diff]
  [:li
   [:p.name (:name diff)]
   [:p (:text diff)]])

(defn semantic-changes-section [name diffs]
  [:div.large-4.medium-4.columns
   [:h3 name]
   (into []
         (concat [:ul]
                 (map #(semantic-change %) diffs)))])

(defn definition [[name sem-diffs]]
  (let [tier-1 (filter #(= (:priority %) 1) sem-diffs)
        tier-2 (filter #(= (:priority %) 2) sem-diffs)
        tier-3 (filter #(= (:priority %) 3) sem-diffs)]
    (layout "FHIRvc | Definition semantic difference"
            [:div.row
             [:h3 (str "Resource Name: " name)]]
            [:div.row
             (semantic-changes-section "Tier 1" tier-1)
             (semantic-changes-section "Tier 2" tier-2)
             (semantic-changes-section "Tier 3" tier-3)])))
             
             
