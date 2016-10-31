(ns fhirvc.generator-test
  (:require [clojure.test :refer :all]
            [fhirvc.generator :refer [edn-tree tree-with-class]]))

(def without-nesting {"added" {"a" "hello"
                               "b" "yes"}
                      "removed" {"c" "no"}
                      "changed" {"d" {"prev" "val-1"
                                      "cur" "val-2"}}
                      "unchanged" {"e" "well"
                                   "f" "ball"}})

(def edn-without-nesting
  [:ul.tree
   [:li [:p "e : well"]]
   [:li [:p "f : ball"]]
   [:li [:p.added "a : hello"]]
   [:li [:p.added "b : yes"]]
   [:li [:p.removed "c : no"]]
   [:li [:p.changed "d"]
    [:ul [:li "previous : val-1"] [:li "current : val-2"]]]])                        

(deftest renders-edn-without-nesting
  (is (= (edn-tree without-nesting)
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
  
(def edn-with-nesting [:ul.tree
                       [:li [:p "c"]                             
                        [:ul
                         [:li [:p "a : 1"]]
                         [:li [:p "b : 2"]]
                         [:li [:p "d"]
                          [:ul
                           [:li [:p "b : 2"]]
                           [:li [:p.changed "f"]
                            [:ul [:li "previous : 1"] [:li "current : 3"]]]]]]]])

(deftest renders-edn-with-nesting-in-difference-structure
  (is (= (edn-tree with-nesting)
         edn-with-nesting)))
                      

(def with-nesting-in-added
  {"added" {"a" {"b" 2
                 "c" {"d" 3}}}
   "removed" {}
   "changed" {}
   "unchanged" {}})

(def edn-with-nesting-in-added
  [:ul.tree
   [:li [:p.added "a"]
    [:ul
     [:li [:p "b : 2"]]
     [:li [:p "c"]
      [:ul
       [:li [:p "d : 3"]]]]]]])      

(deftest renders-edn-with-nesting-in-added
  (is (= (edn-tree with-nesting-in-added)
         edn-with-nesting-in-added)))
  
(deftest extends-tree-when-hashmap-is-not-nested
  (is (= (tree-with-class {"a" 3 "b" 4} "my-style")
         [[:li [:p.my-style "a : 3"]]
          [:li [:p.my-style "b : 4"]]])))
          
               
