(ns fhirvc.views-test
  (:require [clojure.test :refer :all]
            [fhirvc.views :refer :all]))

(def without-nesting {"added" {"a" "hello"
                               "b" "yes"}
                      "removed" {"c" "no"}
                      "changed" {"d" {"prev" "val-1"
                                      "cur" "val-2"}}
                      "unchanged" {"e" "well"
                                   "f" "ball"}})

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

(def with-nesting-in-added
  {"added" {"a" {"b" 2
                 "c" {"d" 3}}}
   "removed" {}
   "changed" {}
   "unchanged" {}})

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
         
