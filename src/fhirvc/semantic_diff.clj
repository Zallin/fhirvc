(ns fhirvc.semantic-diff)

(defn create [& {:keys [priority name text]}]
  {:priority priority
   :name name
   :text text})


