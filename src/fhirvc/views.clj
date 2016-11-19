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

(defn el-name [element-def]
  (get (if (diff/is-diff? element-def)
         (diff/unchanged element-def)
         element-def)
       "path"))         
                     
(defn metadata [def]
  (if (diff/is-diff? def)
    (apply diff/create (map #(dissoc % "snapshot" "differential")
                            (diff/enumerate def)))
    (dissoc def "snapshot" "differential")))

(defn base-property [def & keys]
  (let [changed-in-def (diff/changed def)]
    (if-let [property-val (get-in changed-in-def keys)]
      property-val
      (get-in (diff/unchanged def) keys))))      

(defn snapshot [def]
  (base-property def "snapshot" "element"))

(defn differential [def]
  (base-property def "differential" "element"))

(defn vector-concat [& xs]
  (into [] (apply concat xs)))

(defn to-keyval-seq [seqable]
  (if (map? seqable)
    (seq seqable)
    (to-keyval-seq (into {}
                        (map #(vector %1 %2)
                             (range)
                             seqable)))))

(declare vector-diff-repr
         map-diff-repr
         iterate-over)

(defn tree-repr [hm]
  (letfn [(inner [obj]
            (vector-concat [:ul]
                           (map (fn [[key val]]
                                  (if (coll? val)
                                    [:li [:p key]
                                     (inner val)]
                                    [:li [:p (str key " : " val)]]))
                                (to-keyval-seq obj))))]
    (vector-concat [:ul.tree]
                   (rest (inner hm)))))

(defn vector-repr
  ([vec] (vector-repr vec "unchanged"))
  ([vec class]
   (map (fn [val]
          [(keyword (str "li.array-el." class))
           (vector-concat [:ul]
                          (cond (and (coll? val) (diff/is-diff? val)) (map-diff-repr val)
                                (coll? val) (iterate-over val "unchanged")
                                :else val))])
        vec)))

(defn iterate-over [obj class]
  (reduce (fn [acc [key val]]
               (conj acc 
                     (cond (coll? val) [:li [(keyword (str "p." class)) key]
                                        (if (map? val)
                                          (tree-repr val)
                                          (vector-concat [:ul.array] (vector-repr val)))]                                        
                           :else [:li [(keyword (str "p." class)) (str key " : " val)]])))
          []
          (seq obj)))

(defn map-repr [hm]
  (vector-concat [:ul]
                 (iterate-over hm "unchanged")))

(defn vector-diff-repr [vector-difference]
  (vector-concat [:ul.array]
                 (vector-repr (diff/added vector-difference) "added")
                 (vector-repr (diff/removed vector-difference) "removed")
                 (vector-repr (diff/changed vector-difference) "changed")
                 (vector-repr (diff/unchanged vector-difference) "unchanged")))

(defn map-diff-repr [difference]
  (vector-concat [:ul]
                  (iterate-over (diff/added difference) "added")
                  (iterate-over (diff/removed difference) "removed")
                  (reduce (fn [acc [key val]]
                            (conj acc
                                  (cond (diff/is-diff? val) [:li [:p.changed key]
                                                             (if (map? (diff/added val))
                                                               (map-diff-repr val)
                                                               (vector-diff-repr val))]                                 
                                        (map? val)
                                        [:li [:p.changed key]
                                         [:ul
                                          [:li (str "previous : " (get val "prev"))]
                                          [:li (str "current : " (get val "cur"))]]])))
                          []
                          (diff/changed difference))
                  (iterate-over (diff/unchanged difference) "unchanged")))

(defn accordion-item [title cnt]
  [:li.accordion-item {:data-accordion-item ""}
   [:a.accordion-title {:href "#"} title]
   [:ul.accordion-content.element-definition {:data-tab-content ""} cnt]])       

(defn accordion-items-for [repr-fun element-defs]
  (map (fn [element-def]             
           (accordion-item (el-name element-def)
                           (repr-fun element-def)))
       element-defs))

(defn difference-as-accordion [difference]
  (let [[added removed changed unchanged] (diff/enumerate difference)]
    [[:h4.def-header "Added element definitions"]
     (vector-concat [:ul.accordion {:data-accordion "" :data-multi-expand "true" :data-allow-all-closed "true"}]
                    (accordion-items-for map-repr added))
     [:h4.def-header "Removed element definitions"]
     (vector-concat [:ul.accordion {:data-accordion "" :data-multi-expand "true" :data-allow-all-closed "true"}]
                    (accordion-items-for map-repr removed))
     [:h4.def-header "Changed element definitions"]
     (vector-concat [:ul.accordion {:data-accordion "" :data-multi-expand "true" :data-allow-all-closed "true"}]
                    (accordion-items-for map-diff-repr changed))
     [:h4.def-header "Unchanged element definitions"]
     (vector-concat [:ul.accordion {:data-accordion "" :data-multi-expand "true" :data-allow-all-closed "true"}]
                    (accordion-items-for map-repr unchanged))]))
               
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
             (if (diff/is-diff? def)
               (map-diff-repr (metadata def))
               (map-repr (metadata def)))]
            (if (diff/is-diff? def)
              (vector-concat [:div#panel2.tabs-panel]
                             (difference-as-accordion (snapshot def)))
              [:div#panel2.tabs-panel
               (vector-concat [:ul.accordion {:data-accordion "" :data-multi-expand "true" :data-allow-all-closed "true"}]
                              (accordion-items-for map-repr (get-in def ["snapshot" "element"])))])
            (if (diff/is-diff? def)           
              (vector-concat [:div#panel3.tabs-panel]
                             (difference-as-accordion (differential def)))
              [:div#panel3.tabs-panel
               (vector-concat [:ul.accordion {:data-accordion "" :data-multi-expand "true" :data-allow-all-closed "true"}]
                              (accordion-items-for map-repr (get-in def ["differential" "element"])))])]]))



           

