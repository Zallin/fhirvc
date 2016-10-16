(ns fhirvc.comparator
  (:require [clojure.set :refer [intersection difference]]
            [cheshire.core :refer :all]
            [config.core :refer [env]]
            [fhirvc.utils :refer :all]))

(defn get-type [o]
  (if (contains? o "resourceType")
    (get o "resourceType")
    (get-in o ["resource" "resourceType"])))

(defmulti corresponds? (fn [a b] [(get-type a) (get-type b)]))                

(defmethod corresponds? ["StructureDefinition" "StructureDefinition"] [a b]
  (and (= (get a "name") (get b "name"))))

(defmethod corresponds? :default [a b]
  (= a b))

(defn pairs-when [f source-a source-b]
  (for [a source-a
        b source-b
        :when (f a b)]
    [a b]))                    

(defmulti coll-diff (fn [a b] [(type a) (type b)]))

(defmethod coll-diff [clojure.lang.IPersistentVector clojure.lang.IPersistentVector] [a b]
  (let [common (pairs-when corresponds? a b)]
    {:added (into [] (difference (set b) (set (map second common))))
     :removed (into [] (difference (set a) (set (map first common))))
     :unchanged (map first (filter #(= (first %) (second %)) common))
     :changed (map #(coll-diff (first %) (second %)) (filter #(not= (first %) (second %)) common))}))

(defn props-filtered-on [f prop-set a b]
  (->> prop-set
       (map #(vector % (get a %) (get b %)))
       (filter #(f (second %) (nth % 2)))
       (map #(first %))))

(defn changed-keys [keys a b]
  (loop [cur-keys keys
         acc {}]
    (if (empty? cur-keys)
      acc
      (let [cur-key (first cur-keys)]
        (recur (rest cur-keys) (assoc acc cur-key (coll-diff (get a cur-key) (get b cur-key))))))))

(defmethod coll-diff [clojure.lang.IPersistentMap clojure.lang.IPersistentMap] [a b]
  (let [keys-a (set (keys a))
        keys-b (set (keys b))
        common (intersection keys-a keys-b)]
    {:added (select-keys b (difference keys-b keys-a))
     :removed (select-keys a (difference keys-a keys-b))
     :unchanged (select-keys a (props-filtered-on = common a b))
     :changed (changed-keys (props-filtered-on not= common a b) a b)}))

(defmethod coll-diff :default [a b]
  {:prev a :cur b})
