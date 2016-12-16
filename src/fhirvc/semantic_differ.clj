(ns fhirvc.semantic-differ
  (:require [fhirvc.structure-diff :as struct-diff]))

(defn init-semantic-rules [name]
  (let [ns-name (symbol (str "fhirvc.semantic-rules." name))]
    (require ns-name)
    (map #(ns-resolve ns-name %)
         (keys (ns-publics ns-name))))) 

(def semantic-rules {:metadata (init-semantic-rules 'metadata)
                     :structure (init-semantic-rules 'structure)
                     :attributes (init-semantic-rules 'attributes)})

(defn element-definitions-diff [struct-diff]
  (let [eldefs (struct-diff/get-properties struct-diff "snapshot" "element")]
    (if (struct-diff/is-diff? eldefs)
      eldefs
      nil)))

(defn changed-eldefs-seq [struct-diff]  
  (let [element-defs (struct-diff/get-properties struct-diff "snapshot" "element")]
    (if (struct-diff/is-diff? element-defs)
      (struct-diff/changed element-defs)
      nil)))

(defn diff [struct-diff]
  (let [metadata-diff (struct-diff/dissoc-properties struct-diff "snapshot" "differential")
        elem-defs-diff (element-definitions-diff struct-diff)
        changed-eldefs (changed-eldefs-seq struct-diff)]
    (->> (concat (map #(% metadata-diff) (:metadata semantic-rules))
                 (if (nil? elem-defs-diff)
                   (list)
                   (map #(% elem-defs-diff) (:structure semantic-rules)))
                 (if (nil? changed-eldefs)
                   (list)
                   (map (fn [f]
                          (map #(f %) changed-eldefs))
                        (:attributes semantic-rules))))
         flatten
         (remove nil?))))
