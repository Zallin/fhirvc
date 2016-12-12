(ns fhirvc.structure-diff)

(defn create
  ([added removed changed unchanged]
   {"added" added
    "removed" removed
    "unchanged" unchanged
    "changed" changed})
  ([previous-value current-value]
   {"prev" previous-value
    "cur" current-value}))

(defn added [diff]
  (get diff "added"))

(defn removed [diff]
  (get diff "removed"))

(defn changed [diff]
  (get diff "changed"))

(defn unchanged [diff]
  (get diff "unchanged"))

(defn name [diff]
  (if (contains? diff "name")
    (get diff "name")  
    (get-in diff ["unchanged" "name"])))             

(defn type [diff]
  (if (contains? diff "resourceType")
    (get diff "resourceType")
    (get-in diff ["unchanged" "resourceType"])))

(defn enumerate [diff]
  (if-let [prev (get diff "prev")]
    (list prev (get diff "cur"))
    (list (added diff)
          (removed diff)
          (changed diff)
          (unchanged diff))))

(defn is-diff? [obj]
  (if (coll? obj)  
    (or (contains? obj "prev")
        (contains? obj "added"))
    false))

(defn previous [obj]
  (get obj "prev"))

(defn current [obj]
  (get obj "cur"))

(defn get-property [struct-diff prop]
  (first (remove nil?
                 (map #(get % prop)
                      (enumerate struct-diff)))))

(defn get-properties [struct-diff & props]
  (reduce (fn [r p]
            (if (is-diff? r)
              (get-property r p)
              (get r p)))
          struct-diff
          props))

(defn dissoc-property [struct-diff prop]
  (apply create (map #(dissoc % prop) (enumerate struct-diff))))

(defn dissoc-properties [struct-diff & props]
  (reduce (fn [m p] (dissoc-property m p)) struct-diff props))
