(ns fhirvc.comparator-test
  (:require [clojure.test :refer :all]
            [fhirvc.comparator :refer :all]))

(def a [{"resource" {"resourceType" "StructureDefinition"}
         "name" "first"
         "prop-a" "val-a"}
        {"resource" {"resourceType" "StructureDefinition"}
         "name" "second"
         "prop-a" "val-a"}
        {"resource" {"resourceType" "StructureDefinition"}
         "name" "third"
         "prop-a" "val-a"}])

(def b [{"resource" {"resourceType" "StructureDefinition"}
         "name" "fourth"
         "prop-a" "val-a"}
        {"resource" {"resourceType" "StructureDefinition"}
         "name" "second"
         "prop-a" "val-a"}
        {"resource" {"resourceType" "StructureDefinition"}
         "name" "third"
         "prop-a" "val-b"}])

(def res-1 {"added" [{"resource" {"resourceType" "StructureDefinition"}
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
  (is (= (coll-diff a b)
         res-1)))

(deftest finds-correspondence-between-objs
  (def obj-1 {"resource" {"resourceType" "StructureDefinition"}
              "name" "first"
              "prop-a" "val-a"})
  (def obj-2 {"resource" {"resourceType" "StructureDefinition"}
              "name" "first"
              "prop-d" "val-c"})
  (is (corresponds? obj-1 obj-2)))
       
      
