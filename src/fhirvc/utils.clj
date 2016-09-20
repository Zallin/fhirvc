(ns fhirvc.utils)

(defn filename [path]
  (last (re-seq #"\/?[^/]+" path)))
