(ns fhirvc.views
  (:require [hiccup.core :as hc]
            [hiccup.page :as page]
            [clojure.string :refer [lower-case]]
            [json-html.core :refer :all]
            [config.core :refer [env]]))

(defn append-pref [ref]
  (str (:path-prefix env) ref))



(defn layout [title & cnt]
  (hc/html
   [:html
    [:head
     [:title title]
     (page/include-css (append-pref "public/css/foundation.min.css")
                       (append-pref "public/css/styles.css")
                       "//cdnjs.cloudflare.com/ajax/libs/jstree/3.3.3/themes/default/style.min.css")]
    [:body
     [:div.top-bar
      [:div.top-bar-left
       [:ul.dropdown.menu
        [:li.menu-text "FHIR version comparator"]]]]
     [:div cnt]
     (page/include-js "//code.jquery.com/jquery-3.1.1.min.js"
                      "//cdnjs.cloudflare.com/ajax/libs/jstree/3.3.3/jstree.min.js"
                      (append-pref "public/js/script.js")
                      (append-pref "public/js/jstree.min.js"))]]))

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

(defn definition [def]
  (let [[type name cnt] def]
    (layout "FHIRvc | Definition"
            [:div.row
             [:h3 (str "Resource type: " type)]
             [:h3 (str "Resource name: " name)]]
            [:div.row cnt])))
