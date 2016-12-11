(ns fhirvc.semantic-differ-test
  (:require [clojure.test :refer :all]
            [fhirvc.semantic-differ :as sem-differ]
            [fhirvc.semantic-diff :as sem-diff]
            [fhirvc.structure-diff :as struct-diff]))

(deftest returns-nil-when-there-are-no-structure-changes
  (let [structure-definition-without-changes
        (struct-diff/create {} {} {}
                            {"name" "Account"})]                           
    (is (= (sem-differ/diff structure-definition-without-changes)
           (list)))))

(deftest returns-nil-when-there-are-no-semantic-changes
  (let [structure-definition-without-semantic-changes
        (struct-diff/create {"derivation" "constraint"
                             "baseDefinition" "ref"} 
                            {"base" "ref"
                             "constrainedType" "typeName"}
                            {}
                            {"name" "Account"})]                             
    (is (= (sem-differ/diff structure-definition-without-semantic-changes)
           (list)))))

(defn structure-definition-with [added removed changed unchanged]
  (struct-diff/create {} {} {"snapshot" (struct-diff/create {} {} {"element" (struct-diff/create added
                                                                                                 removed
                                                                                                 changed
                                                                                                 unchanged)} {})} {}))
(deftest identifies-element-definition-addition
  (let [sd (structure-definition-with [{"path" "Account.owner"
                                       "definition" "owner of the account"
                                       "min" 0
                                       "max" 1}] [] [] [])]                                     
    (is (= (sem-differ/diff sd)
           (list (sem-diff/create :priority 1 :name "Account.owner" :text "added"))))))

(deftest identifies-element-definition-removal
  (let [sd (structure-definition-with []
                                      [{"path" "Account.owner"
                                        "definition" "owner of the account"
                                        "min" 0
                                        "max" 1}]
                                      []
                                      [])]                                          
    (is (= (sem-differ/diff sd)
           (list (sem-diff/create :priority 1 :name "Account.owner" :text "removed"))))))

(deftest identifies-element-definition-rename
  (let [sd (structure-definition-with [{"path" "Account.active"
                                        "min" 0
                                        "max" "1"}]
                                      [{"path" "Account.activePeriod"                                                   
                                        "min" 0
                                        "max" 1}]
                                      []
                                      [])]
    (is (= (sem-differ/diff sd)
           (list (sem-diff/create :priority 1 :name "Account.active" :text "Renamed from Account.activePeriod to Account.active"))))))

(deftest identifies-type-change-in-element-definition
  (let [sd (structure-definition-with []
                                      []
                                      [(struct-diff/create {}
                                                           {}
                                                           {"type" (struct-diff/create [{"code" "Money"}]
                                                                                       [{"code" "Quantity"}]
                                                                                       []
                                                                                       [])}
                                                           {"path" "Account.balance"})]
                                      [])]
    (is (= (sem-differ/diff sd)
           (list (sem-diff/create :priority 1 :name "Account.balance" :text "Type changed from Quantity to Money"))))))

(deftest identifies-cardinality-change-in-element-definition 
  (let [sd (structure-definition-with []
                                      []
                                      [(struct-diff/create {} {} {"max" (struct-diff/create 1 "*")} {"path" "Account.status" "min" 1})]
                                      [])]                                                                                                         
    (is (= (sem-differ/diff sd)
           (list (sem-diff/create :priority 1 :name "Account.status" :text "Max cardinality changed from 1 to *"))))))

(deftest identifies-binding-change
  (let [sd (structure-definition-with []
                                      []
                                      [(struct-diff/create {}
                                                           {}
                                                           {"binding" (struct-diff/create {}
                                                                                          {}
                                                                                          {"strength" (struct-diff/create "preferred" "required")}
                                                                                          {"valueSetReference" {"reference" "some_ref"}})}
                                                           {"path" "Account.status"})]
                                      [])]
    (is (= (sem-differ/diff sd)
           (list (sem-diff/create :priority 2 :name "Account.status" :text "Binding strength changed from preferred to required"))))))

(deftest identifies-short-definition-change
  (let [sd (structure-definition-with []
                                      []
                                      [(struct-diff/create {}
                                                           {}
                                                           {"short" (struct-diff/create "old" "new")}
                                                           {"path" "AllergyIntolerance.status"})]
                                      [])]
    (is (= (sem-differ/diff sd)
           (list (sem-diff/create :priority 3 :name "AllergyIntolerance.status" :text "Definition for xml presentation changed from old to new"))))))
                                                                                                                      
                                                                                                 
                                                                  
            
        
