(ns fhirvc.semantic-diff)

(defn create [& {:keys [priority name text]}]
  {:priority priority
   :name name
   :text text})

(defn priority [diff]
  (:priority diff))

(defn name [diff]
  (:name diff))

(defn text [diff]
  (:text diff))
