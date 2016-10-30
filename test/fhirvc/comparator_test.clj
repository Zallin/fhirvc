(ns fhirvc.comparator-test
  (:require [clojure.test :refer :all]
            [fhirvc.comparator :refer :all]))

(def initial-a
  [{"resource" {"resourceType" "StructureDefinition"}
    "name" "first"
    "prop-a" "val-a"}
   {"resource" {"resourceType" "StructureDefinition"}
    "name" "second"
    "prop-a" "val-a"}
   {"resource" {"resourceType" "StructureDefinition"}
    "name" "third"
    "prop-a" "val-a"}])

(def initial-b
  [{"resource" {"resourceType" "StructureDefinition"}
    "name" "fourth"
    "prop-a" "val-a"}
   {"resource" {"resourceType" "StructureDefinition"}
    "name" "second"
    "prop-a" "val-a"}
   {"resource" {"resourceType" "StructureDefinition"}
    "name" "third"
    "prop-a" "val-b"}])

(def res-without-nesting
  {"added" [{"resource" {"resourceType" "StructureDefinition"}
             "name" "fourth"
             "prop-a" "val-a"}]
   "removed" [{"resource" {"resourceType" "StructureDefinition"}
               "name" "first"
               "prop-a" "val-a"}]
   "unchanged" [{"resource" {"resourceType" "StructureDefinition"}
                 "name" "second"
                 "prop-a" "val-a"}]
   "changed" [{"added" {}
               "removed" {}
               "unchanged" {"resource" {"resourceType" "StructureDefinition"}
                            "name" "third"}
               "changed" {"prop-a" {"prev" "val-a" "cur" "val-b"}}}]})

(deftest identifies-types-of-changes-in-vector
  (is (= (coll-diff initial-a initial-b)
         res-without-nesting)))

(def initial-nested-a
  {"c" {"a" 1
        "b" 2
        "d" {"f" 1
             "b" 2}}})

(def initial-nested-b
  {"c" {"a" 1
        "b" 2
        "d" {"f" 3
             "b" 2}}})  

(def res-with-nesting
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

(deftest diff-between-nested-maps
  (is (= (coll-diff initial-nested-a initial-nested-b)
         res-with-nesting)))

(deftest finds-correspondence-between-objs
  (def obj-1 {"resource" {"resourceType" "StructureDefinition"}
              "name" "first"
              "prop-a" "val-a"})
  (def obj-2 {"resource" {"resourceType" "StructureDefinition"}
              "name" "first"
              "prop-d" "val-c"})
  (is (corresponds? obj-1 obj-2)))
       
      
