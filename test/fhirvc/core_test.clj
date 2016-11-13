(ns fhirvc.core-test
  (:require [clojure.test :refer :all]
            [fhirvc.core :refer :all]))

(deftest finds-pairs
  (is (= (pairs ["a" "b" "c"])
         [["b" "a"] ["c" "a"] ["c" "b"]])))

(def fhir-pair
  [{:name "a"
    :data [{"resourceType" "StructureDefinition"
            "other-prop" "other-val"}
           {"resourceType" "OperationDefinition"
            "key" "val"}
           {"no-prop" "val"}]}
   {:name "b"
    :data [{"resourceType" "StructureDefinition"
            "other-prop" "other-val"}
           {"resourceType" "OperationDefinition"
            "key" "val"}
           {"no-prop" "val"}]}])
   
(deftest filters-maps-on-key-value
  (is (= (filter-defs-on "resourceType" "StructureDefinition" fhir-pair)
         [{:name "a"
           :data [{"resourceType" "StructureDefinition"
                   "other-prop" "other-val"}]}
          {:name "b"
           :data [{"resourceType" "StructureDefinition"
                   "other-prop" "other-val"}]}])))
         
  


