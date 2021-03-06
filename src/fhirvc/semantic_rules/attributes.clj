(ns fhirvc.semantic-rules.attributes
  (:require [fhirvc.structure-diff :as struct-diff]))

(defn- if-changed [eldef props f]
  (let [val (apply struct-diff/get-vals eldef props)]
    (if (struct-diff/is-diff? val)
      (f (struct-diff/get-val eldef "path") val)
      nil)))         

(defn type-change [eldef]
  (if-changed eldef ["type"]
              (fn [def-name type-value]
                (let [old-type (first (struct-diff/removed type-value))
                      new-type (first (struct-diff/added type-value))]
                  (if (and (= (count old-type) 1)
                           (= (count new-type) 1))
                    {:priority 1
                     :name def-name
                     :text (str "Type changed from "
                                (get old-type "code")
                                " to "
                                (get new-type "code"))}
                     nil)))))                                   

(defn binding-strength-change [eldef]
  (if-changed eldef ["binding" "strength"]
              (fn [def-name binding-value]
                {:priority 2
                 :name def-name
                 :text (str "Binding strength changed from "
                            (struct-diff/previous binding-value)
                            " to "
                            (struct-diff/current binding-value))})))                                

(defn cardinality-change [eldef]
  (list (if-changed eldef ["min"]
                    (fn [def-name min-value]
                      {:priority 1
                       :name def-name
                       :text (str "Min cardinality changed from "
                                  (struct-diff/previous min-value)
                                  " to "
                                  (struct-diff/current min-value))}))
        (if-changed eldef ["max"]
                    (fn [def-name max-value]
                      {:priority 1
                       :name def-name
                       :text (str "Max cardinality changed from "
                                  (struct-diff/previous max-value)
                                  " to "
                                  (struct-diff/current max-value))}))))

(defn short-definition-change [eldef]
  (if-changed eldef ["short"]                     
              (fn [def-name short-value]
                {:priority 3
                 :name def-name
                 :text (str "Definition for xml presentation changed from "
                            (struct-diff/previous short-value)
                            " to "
                            (struct-diff/current short-value))})))
