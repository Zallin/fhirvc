(ns fhirvc.structure-test
  (:require [clojure.test :refer :all]
            [fhirvc.structure :refer :all]))

(deftest diffs-vectors-without-nesting
  (let [a [{"resource" {"resourceType" "StructureDefinition"}
            "name" "first"
            "prop-a" "val-a"}
           {"resource" {"resourceType" "StructureDefinition"}
            "name" "second"
            "prop-a" "val-a"}
           {"resource" {"resourceType" "StructureDefinition"}
            "name" "third"
            "prop-a" "val-a"}]
        b [{"resource" {"resourceType" "StructureDefinition"}
            "name" "fourth"
            "prop-a" "val-a"}
           {"resource" {"resourceType" "StructureDefinition"}
            "name" "second"
            "prop-a" "val-a"}
           {"resource" {"resourceType" "StructureDefinition"}
            "name" "third"
            "prop-a" "val-b"}]
        res {"added" [{"resource" {"resourceType" "StructureDefinition"}
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
                         "changed" {"prop-a" {"prev" "val-a" "cur" "val-b"}}}]}]
    (is (= (diff a b) res))))

(deftest diffs-maps-with-nesting
  (let [a {"c" {"a" 1
                "b" 2
                "d" {"f" 1
                     "p" 1
                     "b" 2}}}
        b {"c" {"a" 1
                "b" 2
                "d" {"f" 3
                     "p" 4
                     "b" 2}}}
        res {"added" {}
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
                                                             "cur" 4}}}}}}}]           
    (is (= (diff a b) res))))

(deftest diffs-maps-with-nested-vectors
  (let [a {"a" [{"b" {"b" 1}
                 "resource" {"resourceType" "StructureDefinition"}
                 "name" "some"}]}
        b {"a" [{"resource" {"resourceType" "StructureDefinition"}
                 "name" "other"
                 "a" {"b" 1}}        
                {"resource" {"resourceType" "StructureDefinition"}
                 "name" "some"
                 "b" {"b" 2}}]}
        res {"added" {}
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
                              "unchanged" []}}}]                
    (is (= (diff a b) res))))

(deftest finds-correspondence-between-element-definitions
  (let [a {"path" "Device.id"
           "short" "some text"
           "min" 0
           "max" 1}
        b {"path" "Device.id"
           "short" "some other text"
           "min" 1
           "max" 0}] 
    (is (corresponds? a b))))

(deftest finds-correspondence-between-structure-definitions
  (let [a {"resourceType" "StructureDefinition"
           "name" "first"
           "prop-a" "val-a"}
        b {"resourceType" "StructureDefinition"
           "name" "first"
           "prop-d" "val-c"}]
    (is (corresponds? a b))))


