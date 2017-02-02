(ns fhirvc.semantics
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
  (let [eldefs (struct-diff/get-vals struct-diff "snapshot" "element")]
    (if (struct-diff/is-diff? eldefs)
      eldefs
      nil)))

(defn diff [struct-diff]
  (let [metadata (struct-diff/dissoc-keys struct-diff "snapshot" "differential")
        eldefs (element-definitions-diff struct-diff)
        changed-eldefs (if eldefs (struct-diff/changed eldefs))]
    (->> (concat (map #(% metadata) (:metadata semantic-rules))
                 (if eldefs (map #(% eldefs) (:structure semantic-rules)) [])
                 (map (fn [e] (map #(% e) (:attributes semantic-rules))) changed-eldefs))
         flatten
         (remove nil?))))
