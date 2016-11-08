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
             "p" 1
             "b" 2}}})

(def initial-nested-b
  {"c" {"a" 1
        "b" 2
        "d" {"f" 3
             "p" 4
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
                                                   "cur" 3}
                                              "p" {"prev" 1
                                                   "cur" 4}}}}}}})

(deftest diff-between-nested-maps
  (is (= (coll-diff initial-nested-a initial-nested-b)
         res-with-nesting)))

(def obj-with-vec-a
  {"a" [{"b" {"b" 1}
         "resource" {"resourceType" "StructureDefinition"}
         "name" "some"}]})

(def obj-with-vec-b
  {"a" [{"resource" {"resourceType" "StructureDefinition"}
         "name" "other"
         "a" {"b" 1}}        
        {"resource" {"resourceType" "StructureDefinition"}
         "name" "some"
         "b" {"b" 2}}]})
      
(def vec-diff
  {"added" {}
   "removed" {}
   "unchanged" {}
   "changed" {"a"  {"added" [{"resource" {"resourceType" "StructureDefinition"}
                              "name" "other"
                              "a" {"b" 1}}]
                    "removed" []
                    "changed" [{"added" {}
                                "removed" {}
                                "changed" {"b" {"added" {}
                                                "removed" {}
                                                "changed" {"b" {"prev" 1
                                                                "cur" 2}}
                                                "unchanged" {}}}
                                "unchanged" {"resource" {"resourceType" "StructureDefinition"}
                                             "name" "some"}}]
                    "unchanged" []}}})

(deftest finds-diffs-between-objects-with-vectors
  (is (= (coll-diff obj-with-vec-a obj-with-vec-b)
         vec-diff)))

(def el-def-1
  {"path" "Device.id"
   "short" "some text"
   "min" 0
   "max" 1})

(def el-def-2
  {"path" "Device.id"
   "short" "some other text"
   "min" 1
   "max" 0})

(deftest finds-correpondence-between-el-defs
  (is (corresponds? el-def-1 el-def-2)))

(deftest finds-correspondence-between-objs
  (def obj-1 {"resourceType" "StructureDefinition"
              "name" "first"
              "prop-a" "val-a"})
  (def obj-2 {"resourceType" "StructureDefinition"
              "name" "first"
              "prop-d" "val-c"})
  (is (corresponds? obj-1 obj-2)))
