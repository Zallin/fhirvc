(ns fhirvc.structure-diff-test
  (:require [clojure.test :refer :all]
            [fhirvc.structure-diff :refer :all]))

(deftest determines-struct-diff
  (is (= (is-diff? {"added" {}
                    "removed" {}
                    "changed" {}
                    "unchanged" {}})
         true)))

(deftest determines-degenerative-struct-diff
  (is (= (is-diff? {"prev" 1 "cur" 2})
         true)))

(deftest enumerates-struct-diff
  (is (= (enumerate {"added" {"a" 1}
                     "removed" {}
                     "changed" {"b" {"prev" 0
                                     "cur" 1}}
                     "unchanged" {}})
         (list {"a" 1} {} {"b" {"prev" 0 "cur" 1}} {}))))

(deftest enumerates-degenerate-struct-diff
  (is (= (enumerate {"prev" 1 "cur" 2})
         (list 1 2))))                 

(deftest returns-false-for-random-hashmap
  (is (= (is-diff? {"hello" "well?" "this is" "dog"})
         false)))

(deftest dissocs-properties
  (is (= (dissoc-properties {"added" {"a" 1}
                             "removed" {"b" 2
                                        "c" 3}
                             "changed" {"d" {"prev" 1
                                             "cur" "1"}
                                        "f" {"prev" 1
                                             "cur" 2}}
                             "unchanged" {}}
                            "a" "b" "d")                          
         {"added" {}
          "removed" {"c" 3}
          "changed" {"f" {"prev" 1
                          "cur" 2}}
          "unchanged" {}})))

(deftest dissocs-single-property
  (is (= (dissoc-property {"added" {"a" 5
                                    "b" 6}
                           "removed" {}
                           "changed" {}
                           "unchanged" {}}
                          "a")
         {"added" {"b" 6}
          "removed" {}
          "changed" {}
          "unchanged" {}})))

(deftest gets-properties-in-depth
  (is (= (get-properties {"added" {}
                          "removed" {}
                          "changed" {"a" {"added" {"b" 3}
                                          "removed" {}
                                          "changed" {}
                                          "unchanged" {}}}
                          "unchanged" {}}
                         "a" "b")
         3)))

                   
(deftest gets-single-property
  (is (= (get-property {"added" {"a" 1}
                        "removed" {}
                        "changed" {}
                        "unchanged" {}}
                       "a")
         1)))
         
