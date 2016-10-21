(ns fhirvc.generator
  (:require [fhirvc.views :refer :all]
            [fhirvc.adt :refer :all]
            [clojure.java.io :as io]
            [clojure.string :refer [replace]]))

(defn generate-index [output-folder comp-seq]
  (spit (str output-folder "/index.html") (index comp-seq)))

(defn generate-comparison-page [output-folder comp]
  (spit (str output-folder "/" (comp-ref comp))
        (version-comparison comp)))

; move to utils

(defn mkdir [folder-name]
  (.mkdir (java.io.File. folder-name)))

(defn generate-definition-page [output-folder comp]
  (let [[a b] (fhir-names comp)
        diff (fhir-diff comp)
        folder-name (str output-folder "/" (replace (comp-ref comp) "_page.html" "" ))]
    (mkdir folder-name)
    (map #(spit (str folder-name "/" (def-name %) ".html") (definition %))
         (concat (added diff)
                 (removed diff)
                 (changed diff)
                 (unchanged diff)))))
        
;; add public folder copy                    
(defn generate-site [output-folder comp-seq]
  (mkdir output-folder)
  (generate-index output-folder comp-seq)
  (map (fn [comp]
         (generate-comparison-page output-folder comp)
         (generate-definition-page output-folder comp))
       comp-seq))
