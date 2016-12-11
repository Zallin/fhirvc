(ns fhirvc.semantic-rules.attributes
  (:require [fhirvc.structure-diff :as struct-diff]
            [fhirvc.semantic-diff :as sem-diff]))

(defn- if-changed [eldef props f]
  (let [val (apply struct-diff/get-properties eldef props)]
    (if (struct-diff/is-diff? val)
      (f (struct-diff/get-property eldef "path") val)
      nil)))         

(defn type-change [eldef]
  (if-changed eldef ["type"]
              (fn [def-name type-value]
                (let [old-type (first (struct-diff/removed type-value))
                      new-type (first (struct-diff/added type-value))]
                  (if (and (= (count old-type) 1)
                           (= (count new-type) 1))        
                    (sem-diff/create :priority 1
                                     :name def-name
                                     :text (str "Type changed from "
                                                (get old-type "code")
                                                " to "
                                                (get new-type "code")))
                    nil)))))
                    
                


(defn binding-strength-change [eldef]
  (if-changed eldef ["binding" "strength"]
              (fn [def-name binding-value]
                (sem-diff/create :priority 2
                                 :name def-name
                                 :text (str "Binding strength changed from "
                                            (struct-diff/previous binding-value)
                                            " to "
                                            (struct-diff/current binding-value))))))
                                 
                
                

