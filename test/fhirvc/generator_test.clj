(ns fhirvc.generator-test
  (:require [clojure.test :refer :all]
            [fhirvc.generator :refer [html-diff-repr]]))

(def without-nesting {"added" {"a" "hello"
                               "b" "yes"}
                      "removed" {"c" "no"}
                      "changed" {"d" {"prev" "val-1"
                                      "cur" "val-2"}}
                      "unchanged" {"e" "well"
                                   "f" "ball"}})

(def edn-without-nesting
  [:div.row
   [:ul
    [:h4 "Added properties"]
    [:li [:p "a : hello"]]
    [:li [:p "b : yes"]]]
   [:ul
    [:h4 "Removed properties"]    
    [:li [:p "c : no"]]]
   [:ul
    [:h4 "Changed properties"]    
    [:li [:p "d"]
     [:ul
      [:li [:p "previous : val-1"]]
      [:li [:p "current : val-2"]]]]]
   [:ul
    [:h4 "Unchanged properties"]
    [:li [:p "e : well"]]
    [:li [:p "f : ball"]]]])
  
(deftest renders-edn-without-nesting
  (is (= (html-diff-repr without-nesting)
         edn-without-nesting)))
  
(def with-nesting
    {"added" {}
     "removed" {}
     "unchanged" {}
     "changed" {"c" {"added" {}
                     "removed" {}
                     "unchanged" {"a" 1
                                  "b" 2}
                     "changed" {"d" {"added" {}
                                     "removed" {}
                                     "unchanged" {"b" 2}
                                     "changed" {"f" {"prev" 1
                                                     "cur" 3}}}}}}})
  
(def edn-with-nesting [:div.row
                       [:ul
                        [:h4 "Added properties"]]
                       [:ul
                        [:h4 "Removed properties"]]
                       [:ul
                        [:h4 "Changed properties"]
                        [:li [:p "c.d.f"]
                         [:ul
                          [:li [:p "previous : 1"]]
                          [:li [:p "current : 3"]]]]]
                       [:ul
                        [:h4 "Unchanged properties"]
                        [:li [:p "c.a : 1"]]
                        [:li [:p "c.b : 2"]]
                        [:li [:p "c.d.b : 2"]]]])                            
                       
(deftest renders-edn-with-nesting-in-difference-structure
  (is (= (html-diff-repr with-nesting)
         edn-with-nesting)))                      

(def with-nesting-in-added
  {"added" {"a" {"b" 2
                 "c" {"d" 3}}}
   "removed" {}
   "changed" {}
   "unchanged" {}})

(def edn-with-nesting-in-added
  [:div.row
   [:ul
    [:h4 "Added properties"]
    [:li
     [:ul.tree
      [:li [:p "a"]
       [:ul
        [:li [:p "b : 2"]]
        [:li [:p "c"]
         [:ul
          [:li [:p "d : 3"]]]]]]]]]
   [:ul
    [:h4 "Removed properties"]]
   [:ul
    [:h4 "Changed properties"]]
   [:ul
    [:h4 "Unchanged properties"]]])

(deftest renders-edn-with-nesting-in-added
  (is (= (html-diff-repr with-nesting-in-added)
         edn-with-nesting-in-added)))               


(def arr-diff
  {"added" {}
   "removed" {}
   "unchanged" {}
   "changed" {"a"  {"added" [{"a" {"b" 1}}]
                    "removed" []
                    "changed" [{"added" {}
                                "removed" {}
                                "changed" {"b" {"added" {}
                                                "removed" {}
                                                "changed" {"b" {"prev" 1
                                                                "cur" 2}}
                                                "unchanged" {}}}
                                "unchanged" []}]}}})

(def arr-diff-html
  [:div.row
   [:ul
    [:h4 "Added properties"]
    [:li
     [:ul.tree
      [:li [:p "a[X].a"]
       [:ul
        [:li [:p "b : 1"]]]]]]]      
   [:ul
    [:h4 "Removed properties"]]
   [:ul
    [:h4 "Changed properties"]
    [:li [:p "a[X].a.b"]
     [:ul
      [:li [:p "previous : 1"]]
      [:li [:p "current : 2"]]]]]          
   [:ul
    [:h4 "Unchanged properties"]]])

(deftest renders-html-for-array-diff
  (is (= (html-diff-repr arr-diff)
         arr-diff-html)))
    
