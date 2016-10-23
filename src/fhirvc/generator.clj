(ns fhirvc.generator
  (:require [fhirvc.views :refer :all]
            [fhirvc.adt :refer :all]
            [clojure.string :refer [replace]]
            [me.raynes.fs :refer [copy-dir mkdir]]))

(defn generate-index [output-folder comp-seq]
  (spit (str output-folder "/index.html") (index comp-seq)))

(defn generate-comparison-page [output-folder comp]
  (spit (str output-folder "/" (comp-ref comp))
        (version-comparison comp)))

(defn generate-definition-page [output-folder comp]
  (let [diff (fhir-diff comp)
        folder-name (str output-folder "/" (replace (comp-ref comp) "_page.html" "" ))]
    (mkdir folder-name)
    (dorun (map #(spit (str folder-name "/" (def-name %) ".html") (definition %))
                (concat (added diff)
                        (removed diff)
                        (changed diff)
                        (unchanged diff))))))

(defn move-static-to [output-folder]
  (copy-dir "resources/static/css" (str output-folder "/css")))

(defn generate-site [output-folder comp-seq]
  (mkdir output-folder)
  (move-static-to output-folder)
  (generate-index output-folder comp-seq)
  (dorun (map (fn [comp]
                (generate-comparison-page output-folder comp)
                (generate-definition-page output-folder comp))
              comp-seq)))
