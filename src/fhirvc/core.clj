(ns fhirvc.core
  (:require [config.core :refer [env]]
            [cheshire.core :refer :all]
            [clojure.java.io :refer [file]]
            [fhirvc.structure-differ :refer [coll-diff]]
            [fhirvc.generator :refer [generate-site]]
            [fhirvc.fhir-comparison :as comp]
            [clojure.string :refer [trim]]))

(defn pairs [versions]
  (for [a versions
        b versions
        :while (not= a b)]
    [a b]))

(defn readdir [dir]
  (map slurp (rest (file-seq (file dir)))))

(defn files [[a b]]
  (let [data-a (readdir (str (:fhir-folder env) "/" a))
        data-b (readdir (str (:fhir-folder env) "/" b))]
    [{:name a
      :data data-a}
     {:name b
      :data data-b}]))

(defn update-seq [attr f seq]
  (map #(update % attr f) seq))

(defn to-edn [pair]
  (update-seq :data #(map parse-string %) pair))

(defn definitions [pair]
  (update-seq :data #(into [] (map (fn [el] (get el "resource")) %))
              (update-seq :data #(mapcat (fn [cnt] (get cnt "entry")) %) pair)))

(defn filter-defs-on [key val pair]
  (map (fn [{:keys [name data]}]
         {:name name
          :data (into [] (filter #(= (get % key) val) data))})
       pair))
                      
(defn comparison [[a b]]
  (let [diff (coll-diff (:data b) (:data a))]
    (comp/create (:name a) (:name b) diff)))  

(defn -main [& args]
  (generate-site (:output-folder env) 
                 (map (fn [pair]
                        (->> (files pair)
                             to-edn
                             definitions
                             (filter-defs-on "resourceType" "StructureDefinition")
                             comparison))
                      (pairs (:versions env)))))
