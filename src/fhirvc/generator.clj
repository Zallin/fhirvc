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

(defn vector-concat [& xs]
  (into [] (apply concat xs)))

(defn to-keyval-seq [seqable]
  (if (map? seqable)
    (seq seqable)
    (to-keyval-seq (into {}
                        (map #(vector %1 %2)
                             (range)
                             seqable)))))

(defn update-path [path key]
  (cond (clojure.string/blank? path) key
        (= key "[]") (str path "[X]")
        :else (str path "." key)))
        
(defn tree [obj]
  (letfn [(inner [obj]
            (vector-concat [:ul]
                           (map (fn [[key val]]
                                  (if (coll? val)
                                    [:li [:p key]
                                     (inner val)]
                                    [:li [:p (str key " : " val)]]))
                                (to-keyval-seq obj))))]
    (vector-concat [:ul.tree]
                   (rest (inner obj)))))
                           
(defn repr-with-prefix [path obj]
  (reduce (fn [acc [key val]]
            (conj acc
                  (if (coll? val)
                    [:li (tree {(update-path path key) val})]
                    [:li [:p (str (update-path path key) " : " val)]])))
          []
          (to-keyval-seq obj)))
                    
(defn html-diff-repr
  ([difference]
   (let [[added removed changed unchanged] (html-diff-repr "" difference)]
     [:div.row
      (vector-concat [:ul
                      [:h4 "Added properties"]]
                     added)
      (vector-concat [:ul
                      [:h4 "Removed properties"]]
                     removed)
      (vector-concat [:ul
                      [:h4 "Changed properties"]]
                     changed)
      (vector-concat [:ul
                      [:h4 "Unchanged properties"]]
                     unchanged)]))
  
  ([path difference]
   (map vector-concat        
        (reduce (fn [acc cur-val]
                  (if (vector? cur-val)
                    (let [[key val] cur-val]
                      (if (diff/is-diff? val)
                        (map vector-concat acc (html-diff-repr (update-path path key) val))
                        (update (vec acc) 2 #(conj % [:li [:p (update-path path key)]
                                                [:ul
                                                 [:li [:p (str "previous : " (diff/previous val))]]
                                                 [:li [:p (str "current : " (diff/current val))]]]]))))
                    (map vector-concat acc (html-diff-repr (update-path path "[]") cur-val))))
                [[] [] [] []]
                (seq (diff/changed difference)))
        [(repr-with-prefix path (diff/added difference))
         (repr-with-prefix path (diff/removed difference))
         []
         (repr-with-prefix path (diff/unchanged difference))])))

; изменить то как отображаются 
(defn definition-view-data [difference]
  (vector (diff/type difference)
          (diff/name difference)
          (html-diff-repr difference)))

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
  (copy-dir "resources/public" (str output-folder "/public")))

(defn generate-site [output-folder comp-seq]
  (mkdir output-folder)
  (move-static-to output-folder)
  (generate-index output-folder comp-seq)
  (dorun (map (fn [comp]
                (generate-comparison-page output-folder comp)
                (generate-definition-pages output-folder comp))
              comp-seq)))


