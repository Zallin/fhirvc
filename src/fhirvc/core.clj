(ns fhirvc.core
  (:require [fhirvc.structure :as structure]
            [fhirvc.structure-diff :as struct-diff]
            [fhirvc.semantics :as semantics]
            [fhirvc.generator :refer [generate-site]]
            [cheshire.core :refer :all]                        
            [me.raynes.fs :as fs]))

(defn definitions [bundle]
  (map #(get % "resource") (get bundle "entry")))

(defn is-structure-definition [v]
  (= (get v "resourceType") "StructureDefinition"))

(defn fhir-versions [path]
  (map (fn [d]
         (let [parsed-files (map (comp parse-string slurp) (fs/list-dir d))]           
           {:name (fs/base-name d)
            :data (->> parsed-files
                       (map definitions)
                       (apply concat)
                       (filter is-structure-definition)
                       vec)
            :date (get-in (first parsed-files) ["meta" "lastUpdated"])}))
         (fs/list-dir path)))

(defn version-pairs [versions]
  (for [a versions
        b versions
        :when (< (compare (:date a) (:date b)) 0)]
    [a b]))

(defn structure-difference [[a b]]
  {:old-version (:name a)
   :new-version (:name b)
   :structure-difference (structure/diff (:data a) (:data b))})

(defn semantic-difference [comparison]  
  (assoc comparison
         :semantic-difference (map (fn [str-def]
                                     [(struct-diff/get-val str-def "name")
                                      (semantics/diff str-def)])
                                   (struct-diff/changed (:structure-difference comparison)))))

(defn -main [& args]
  (->> (fhir-versions "resources/fhir")
       version-pairs
       (map structure-difference)
       (map semantic-difference)       
       (generate-site "resources/site")))
