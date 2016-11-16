(ns fhirvc.views
  (:require [fhirvc.diff :as diff]
            [hiccup.core :as hc]
            [hiccup.page :as page]
            [clojure.string :refer [lower-case]]
            [json-html.core :refer :all]
            [config.core :refer [env]]))

(defn prefix [ref]
  (str (:path-prefix env) "public/" ref))

(defn layout [title & cnt]
  (hc/html
   [:html
    [:head
     [:title title]
     (page/include-css (prefix "css/foundation.css")
                       (prefix "css/styles.css"))]
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
           [:h3 "Following versions currenly supported"]]
          [:div.row 
           [:div.large-10.medium-10 
            [:form
             [:div.row
              [:ul
               (for [[a b ref] comp-seq]
                 [:li [:a {:href ref} (str a " to " b)]])]]]]]))

(defn section [header defs]
  (if (empty? defs)
    [:div.row
     [:h4 (str "No " (lower-case header))]]
    [:div.row
     [:h4 header]
     [:ul
      (for [[name ref] defs]
        [:li [:a {:href ref} name]])]]))

(defn version-comparison [comparison]
  (let [[a b added removed changed unchanged]
        comparison]
    (layout "FHIRvc | Comparison summary"
            [:div.row
             [:h3 (str a " compared to " b)]]
            (section "Added definitions" added)
            (section "Removed definitions" removed)
            (section "Changed defitinions" changed)
            (section "Unchanged definitions" unchanged))))

(defn def-type [def]
  (get (if (diff/is-diff? def)
         (diff/unchanged def)
         def)
       "resourceType"))

(defn def-name [def]
  (get (if (diff/is-diff? def)
         (diff/unchanged def)
         def)
       "name"))
                     
(defn metadata [def]
  (if (diff/is-diff? def)
    (apply diff/create (map #(dissoc % "snapshot" "differential")
                            (diff/enumerate def)))
    (dissoc def "snapshot" "differential")))

(defn snapshot [def]
  (let [changed-in-def (diff/changed def)]    
    (if (contains? changed-in-def "snapshot")
      (get (diff/changed (get changed-in-def "snapshot")) "element")
      (diff/create [] [] [] (get-in (diff/unchanged def) ["snapshot" "element"])))))

(defn differential [def])

(defn html-repr [difference])

(defn accordion-item [title cnt]
  [:li.accordion-item {:data-accordion-item ""}
   [:a.accordion-title {:href "#"} title]
   [:ul.accordion-content.element-definition {:data-tab-content ""} cnt]])       

(defn accordion-items-for [element-defs]
  (map (fn [element-def]
         ;; CHANGE!!!
         (let [name (get element-def "path")]
           (accordion-item name
                           (html-repr element-def))))
       element-defs))

(defn difference-as-accordion [difference]
  (let [[added removed changed unchanged] (diff/enumerate difference)]
    [[:h4.def-header "Added definitions"]
     [:ul.accordion {:data-accordion "" :data-multi-expand "true" :data-allow-all-closed "true"}
      (accordion-items-for added)]
     [:h4.def-header "Removed definitions"]
     [:ul.accordion {:data-accordion "" :data-multi-expand "true" :data-allow-all-closed "true"}
      (accordion-items-for removed)]
     [:h4.def-header "Changed definitions"]
     [:ul.accordion {:data-accordion "" :data-multi-expand "true" :data-allow-all-closed "true"}
      (accordion-items-for changed)]
     [:h4.def-header "Unchanged definitions"]
     [:ul.accordion {:data-accordion "" :data-multi-expand "true" :data-allow-all-closed "true"}
      (accordion-items-for unchanged)]]))
               
(defn definition [def]
  (layout "FHIRvc | Definition"
          [:div.row
           [:h3 (str "Resource type: " (def-type def))]
           [:h3 (str "Resource name: " (def-name def))]]
          [:div.row 
           [:ul#example-tabs.tabs {:data-tabs ""}
            [:li.tabs-title.is-active
             [:a {:href "#panel1" :aria-selected "true"} "Metadata"]]
            [:li.tabs-title
             [:a {:href "#panel2"} "Snapshot"]]
            [:li.tabs-title
             [:a {:href "#panel3"} "Differential"]]]
           [:div.tabs-content {:data-tabs-content "example-tabs"}
            [:div#panel1.tabs-panel.is-active                         
             (html-repr (metadata def))]
            (if (diff/is-diff? def)
              (concat [:div#panel2.tabs-panel]
                      (difference-as-accordion (snapshot def)))             
              [:div#panel2.tabs-panel
               [:ul.accordion {:data-accordion "" :data-multi-expand "true" :data-allow-all-closed "true"}
                (accordion-items-for (get-in def ["snapshot" "element"]))]])
            (if (diff/is-diff? def)           
              (concat [:div#panel3.tabs-panel]
                      (difference-as-accordion (differential def)))
              [:div#panel3.tabs-panel
               [:ul.accordion {:data-accordion "" :data-multi-expand "true" :data-allow-all-closed "true"}
                (accordion-items-for (get-in def ["differential" "element"]))]])]]))



           

