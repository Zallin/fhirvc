(ns fhirvc.semantic-rules.structure
  (:require [clojure.string :as str]
            [fhirvc.structure-diff :as struct-diff]))

(defn- element-definition-addition [eldefs]
  (map (fn [eldef]
         {:priority 1
          :name (get eldef "path")
          :text "added"})
       eldefs))

(defn- element-definition-removal [eldefs]
  (map (fn [eldef]
         {:priority 1
          :name (get eldef "path")
          :text "removed"})
       eldefs))

(defn- element-definition-renaming [eldefs]
  (map (fn [[a b]]
         (let [old-name (get a "path")
               new-name (get b "path")]
             {:priority 1
              :name new-name
              :text (str "Renamed from " old-name " to " new-name)}))
       eldefs))

;; move to semantics.clj

(defn- was-renamed [old new]
  (let [old-name (get old "path")
        new-name (get new "path")]
    (or (str/includes? old-name new-name)
        (str/includes? new-name old-name))))

(defn- renamed-definitions [eldefs]
  (for [a (struct-diff/added eldefs)
        r (struct-diff/removed eldefs)
        :when (was-renamed r a)]
    [r a]))

(defn structure-change [eldefs]
  (let [renamed (renamed-definitions eldefs)
        old-names (set (map #(first %) renamed))
        new-names (set (map #(second %) renamed))]
    (list (element-definition-addition (remove #(contains? new-names %) (struct-diff/added eldefs)))
          (element-definition-removal (remove #(contains? old-names %) (struct-diff/removed eldefs)))
          (element-definition-renaming renamed))))                  
