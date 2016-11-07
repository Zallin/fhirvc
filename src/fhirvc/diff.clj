(ns fhirvc.diff)

(defn create [added removed unchanged changed]
  {"added" added
   "removed" removed
   "unchanged" unchanged
   "changed" changed})

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
  (vector (added diff)
          (removed diff)
          (changed diff)
          (unchanged diff)))

(defn is-diff? [obj]
  (contains? obj "added"))

(defn previous [obj]
  (get obj "prev"))

(defn current [obj]
  (get obj "cur"))
