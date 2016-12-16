(ns fhirvc.generator
  (:require [fhirvc.views :as views]
            [fhirvc.filenames :as filenames]
            [me.raynes.fs :as fs]))

(defn generate-page [path-to-page view data]
  (spit path-to-page (view data)))


; transfer logic
(defn generate-index [output-dir comps]  
  (generate-page (str output-dir "/index.html")
                 views/index
                 (map (fn [comp]
                        (let [a (:old-version comp)
                              b (:new-version comp)]                          
                          [a b (filenames/comparison a b)]))
                      comps)))

(defn generate-summary-page [output-dir comparison]
  (generate-page (str output-dir "/" (filenames/comparison (:old-version comparison)
                                                           (:new-version comparison)))
                 views/summary
                 comparison))

(defn generate-definition-page [output-dir sem-difference]
  (generate-page (str output-dir "/" (filenames/definition sem-difference))
                 views/definition
                 sem-difference))

(defn generate-definition-pages [output-dir comparison]
  (let [comp-dir (str output-dir "/" (filenames/comparison-dir (:old-version comparison)
                                                               (:new-version comparison)))]  
    (fs/mkdir comp-dir)
    (dorun (map (partial generate-definition-page comp-dir) (:semantic-difference comparison)))))

(defn move-static-to [output-dir]
  (fs/copy-dir "resources/public" (str output-dir "/public")))

(defn generate-site [output-dir comp-seq]
  (if (fs/exists? output-dir)
    (fs/delete-dir output-dir))
  (fs/mkdir output-dir)
  (move-static-to output-dir)
  (generate-index output-dir comp-seq)
  (dorun (map (fn [comp]
                (generate-summary-page output-dir comp)
                (generate-definition-pages output-dir comp))
              comp-seq)))
