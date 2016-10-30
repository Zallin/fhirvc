(ns fhirvc.generator
  (:require [fhirvc.views :as views]
            [fhirvc.diff :as diff]
            [fhirvc.comp :as comp]
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

(defn tree-for [seqable initial label-node label-leaf next-level-f]
  (reduce (fn [cur-tree [prop val]]
            (conj cur-tree (if (seq? val)
                             [:li
                              (label-node prop)
                              (tree-for val initial label-node label-leaf)]
                             [:li (label-leaf prop val)])))
          initial
          (seq seqable)))

(defn hm-tree [hm]
  (tree-for hm [:ul] #([:p %]) #([:p (str %1 " : " %2)])))

(defn ext-with-hashmap [hm style-class tree]
  (tree-for hm
            tree
            #(vector (keyword (str "p." style-class)) %)
            #(vector (keyword (str "p." style-class)) (str %1 " : " %2))))
  
(defn ext-with-diff [difference])

(defn diff-tree [difference]
  (->> [:ul]
       (ext-with-hashmap (diff/added difference) "added")
       (ext-with-hashmap (diff/removed difference) "removed")
       (ext-with-hashmap (diff/unchanged difference) "unchanged")
       (ext-with-diff (diff/changed difference))))                                                

(defn edn-tree [difference]
  [:ul.tree (rest (diff-tree difference))])

(defn definition-view-data [difference]
  (vector (diff/type difference)
          (diff/name difference)
          (edn-tree difference)))

(defn generate-definition-page [output-folder comparison difference]                                
  (generate-page (str output-folder "/" (comp/diff-ref comparison difference))
                 views/definition
                 (definition-view-data difference)))

(defn generate-definition-pages [output-folder comparison]
  (let [difference (comp/diff comparison)
        folder-name (str output-folder "/" (comp/folder comparison))]
    (mkdir folder-name)
    (dorun (map (partial generate-definition-page output-folder comparison)                   
                (reduce concat (diff/enumerate difference))))))                       

(defn move-static-to [output-folder]
  (copy-dir "resources/static/css" (str output-folder "/css")))

(defn generate-site [output-folder comp-seq]
  (mkdir output-folder)
  (move-static-to output-folder)
  (generate-index output-folder comp-seq)
  (dorun (map (fn [comp]
                (generate-comparison-page output-folder comp)
                (generate-definition-pages output-folder comp))
              comp-seq)))


