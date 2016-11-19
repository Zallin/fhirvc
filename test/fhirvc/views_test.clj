(ns fhirvc.views-test
  (:require [clojure.test :refer :all]
            [fhirvc.views :refer :all]))


(deftest generates-unchanged-keyval-pair
  (is (= (map-diff-repr {"added" {}
                          "removed" {}
                          "changed" {}
                          "unchanged" {"a" 1}})
         [:ul
          [:li [:p.unchanged "a : 1"]]])))

(deftest generates-added-keyval-pair
  (is (= (map-diff-repr {"added" {"a" 1}
                          "removed" {}
                          "changed" {}
                          "unchanged" {}})
         [:ul
          [:li [:p.added "a : 1"]]])))

(deftest generates-vector-diff-from-difference
  (is (= (map-diff-repr {"added" {}
                          "removed" {}
                          "changed" {"a" {"added" [{"b" 2}]
                                          "removed" [{"c" 3}]
                                          "changed" []
                                          "unchanged" [{"d" 4}]}}
                          "unchanged" {}})
         [:ul
          [:li [:p.changed "a"]
           [:ul.array 
            [:li.array-el.added
             [:li [:p.unchanged "b : 2"]]]
            [:li.array-el.removed
             [:li [:p.unchanged "c : 3"]]]
            [:li.array-el.unchanged
             [:li [:p.unchanged "d : 4"]]]]]])))

(deftest generates-unchanged-vector-from-difference
  (is (= (map-diff-repr {"added" {}
                          "removed" {}
                          "changed" {}
                          "unchanged" {"a" [{"a" 1} {"b" 2}]}})
         [:ul
          [:li [:p.unchanged "a"]
           [:ul.array 
            [:li.array-el.unchanged
             [:li [:p.unchanged "a : 1"]]]
            [:li.array-el.unchanged
             [:li [:p.unchanged "b : 2"]]]]]])))                                                             

(deftest generates-added-vector-from-difference
  (is (= (map-diff-repr {"added" {"a" [{"a" 1} {"b" 2}]}
                          "removed" {}
                          "changed" {}
                          "unchanged" {}})
         [:ul
          [:li [:p.added "a"]
           [:ul.array
            [:li.array-el.unchanged
             [:li [:p.unchanged "a : 1"]]]
            [:li.array-el.unchanged
             [:li [:p.unchanged "b : 2"]]]]]])))

(deftest generates-tree-from-difference
  (is (= (map-diff-repr {"added" {}
                          "removed" {}
                          "changed" {}
                          "unchanged" {"a" {"b" 2
                                            "c" 3}}})
         [:ul
          [:li [:p.unchanged "a"]
           [:ul.tree
            [:li [:p "b : 2"]]
            [:li [:p "c : 3"]]]]])))

(deftest generates-changed-key-val-from-difference
  (is (= (map-diff-repr {"added" {}
                          "removed" {}
                          "changed" {"a" {"prev" 1
                                          "cur" 2}}
                          "unchanged" {}})
         [:ul
          [:li [:p.changed "a"]
           [:ul
            [:li "previous : 1"]
            [:li "current : 2"]]]])))             

(deftest finds-metadata
  (is (= (metadata {"a" 1 "b" {"a" 1} "c" [{"a" 1}]
                    "snapshot" {"element" [{"a" 1}]}
                    "differential" {"element" [{"a" 1}]}})
         {"a" 1 "b" {"a" 1} "c" [{"a" 1}]})))

(deftest finds-metadata-in-difference
  (is (= (metadata {"added" {"a" 1}
                    "removed" {"b" 2}
                    "unchanged" {"snapshot" {"element" [{"a" 1}]}}
                    "changed" {"c" {"prev" 1
                                    "cur" 2}
                               "differential" {"added" {}
                                               "removed" {}
                                               "changed" {}
                                               "unchanged" {"element" [{"a" 1}]}}}})
         {"added" {"a" 1}
          "removed" {"b" 2}
          "unchanged" {}
          "changed" {"c" {"prev" 1
                          "cur" 2}}})))          

(deftest finds-snapshot-in-difference-when-it-is-changed
  (is (= (snapshot {"added" {}
                    "removed" {}
                    "unchanged" {}
                    "changed" {"snapshot" {"added" {}
                                           "removed" {}
                                           "unchanged" {}
                                           "changed" {"element" {"added" [{"a" 1}]
                                                                 "removed" [{"b" 2}]
                                                                 "changed" [{"c" {"prev" 3 "cur" 5}}]
                                                                 "unchanged" [{"d" 4}]}}}}})
         {"added" [{"a" 1}]
          "removed" [{"b" 2}]
          "changed" [{"c" {"prev" 3 "cur" 5}}]
          "unchanged" [{"d" 4}]})))

(deftest finds-snapshot-in-difference-when-it-is-unchanged
  (is (= (snapshot {"added" {"some" 1}
                    "removed" {"other" 2}
                    "changed" {"c" {"prev" 1
                                    "cur" 2}}
                    "unchanged" {"snapshot"
                                 {"element" [{"a" 1} {"b" 2}]}}})
         {"added" []
          "removed" []
          "changed" []          
          "unchanged" [{"a" 1} {"b" 2}]})))                                                                          

(deftest identifies-type-of-definition
  (is (= (def-type {"resourceType" "a"
                    "b" 2})
         "a")))

(deftest identifies-type-of-definition-difference
  (is (= (def-type {"added" {}
                    "removed" {"b" 2}
                    "changed" {}
                    "unchanged" {"resourceType" "b"}})
         "b")))

(deftest identifies-name-of-definition
  (is (= (def-name {"name" "Jack"
                    "b" 2})
         "Jack")))

(deftest identifies-name-of-definition-difference
  (is (= (def-name {"added" {}
                    "removed" {"b" 2}
                    "changed" {}
                    "unchanged" {"name" "Bob"}})
         "Bob")))
         
