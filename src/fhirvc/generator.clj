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

(defn to-keyval-seq [seqable]
  (if (map? seqable)
    (seq seqable)
    (map #(vector %1 %2) (range) seqable)))

(defn seq-to-edn [fun seqable]
  (map (fn [[key val]]
         (fun key val))
       (to-keyval-seq seqable)))

(defn concat-into [val & xs]
  (into val
        (apply concat xs)))

(defn tree [struct]
  (seq-to-edn (fn [key val]
                (if (coll? val)
                  [:li [:p key]
                   (concat-into [] [:ul] (tree val))]
                  [:li [:p (str key " : " val)]]))
              struct))

(defn tree-with-class [difference class]
  (seq-to-edn (fn [key val]
                (if (coll? val)
                  [:li [(keyword (str "p." class)) key]
                   (concat-into [] [:ul] (tree val))]
                  [:li [(keyword (str "p." class)) (str key " : " val)]]))
              difference))

(defn diff-tree [difference]
  (concat-into [] [:ul]
               (tree (diff/unchanged difference))
               (mapcat #(apply tree-with-class %)
                       [[(diff/added difference) "added"]
                        [(diff/removed difference) "removed"]])                                                                   
               (seq-to-edn (fn [key val]
                             (if (diff/is-diff? val)
                                [:li [:p key]
                                 (diff-tree val)]
                               [:li [:p.changed key]
                                [:ul
                                 [:li (str "previous : " (get val "prev"))]
                                 [:li (str "current : " (get val "cur"))]]]))
                           (diff/changed difference))))

(defn edn-tree [difference]
  (concat-into [] [:ul.tree]
               (if (diff/is-diff? difference)
                 (rest (diff-tree difference))
                 (tree difference))))

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


