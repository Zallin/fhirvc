(ns fhirvc.generator
  (:require [fhirvc.views :as views]
            [fhirvc.structure-diff :as diff]
            [fhirvc.fhir-comparison :as comp]
            [fhirvc.semantic-differ :as sem-diff]
            [me.raynes.fs :refer [copy-dir mkdir]]))

(defn generate-page
  [path-to-page view data]
  (spit path-to-page (view data)))

(defn index-view-data [comparison]
  (let [[a b] (comp/names comparison)]
    [a b (comp/ref comparison)]))

(defn generate-index [output-folder comp-seq]
  (generate-page (str output-folder "/index.html")
                 views/index
                 (map index-view-data comp-seq)))

(defn name-ref-pair [comparison difference]
  [(diff/name difference) (comp/diff-ref comparison difference)])
  
(defn comparison-view-data [comparison]
  (let [difference (comp/diff comparison)]
    (concat (comp/names comparison)
            (map #(map (partial name-ref-pair comparison) %)
                 (diff/enumerate difference)))))

(defn generate-comparison-page [output-folder comparison]
  (generate-page (str output-folder "/" (comp/ref comparison))
                 views/version-comparison
                 (comparison-view-data comparison)))

(defn generate-definition-page [output-folder comparison difference]                                
  (generate-page (str output-folder "/" (comp/diff-ref comparison difference))
                 views/definition
                 [(diff/get-property difference "name")
                  (sem-diff/diff difference)]))

(defn generate-definition-pages [output-folder comparison]
  (let [difference (comp/diff comparison)
        folder-name (str output-folder "/" (comp/folder comparison))]
    (mkdir folder-name)
    (dorun (map (partial generate-definition-page output-folder comparison)                   
                (reduce concat (diff/enumerate difference))))))                       

(defn move-static-to [output-folder]
  (copy-dir "resources/public" (str output-folder "/public")))

(defn generate-site [output-folder comp-seq]
  (mkdir output-folder)
  (move-static-to output-folder)
  (generate-index output-folder comp-seq)
  (dorun (map (fn [comp]
                (generate-comparison-page output-folder comp)
                (generate-definition-pages output-folder comp))
              comp-seq)))


