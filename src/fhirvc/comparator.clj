(ns fhirvc.comparator
  (:require [clojure.set :refer [intersection difference]]
            [cheshire.core :refer :all]
            [config.core :refer [env]]
            [fhirvc.diff :as diff]))

(defn primary-attrs [map]
  (cond (contains? map "name") ["name"]
        (contains? map "path") ["path"]
        :else (keys map)))

(defn has-attrs? [obj attrs]
  (every? #(contains? obj %) attrs))

(defmulti corresponds? (fn [a b] [(type a) (type b)]))

(defmethod corresponds? [clojure.lang.IPersistentMap clojure.lang.IPersistentMap] [a b]
  (let [attrs (primary-attrs a)]
    (if (has-attrs? b attrs)
      (every? #(= (get a %) (get b %)) attrs)
      false)))

(defmethod corresponds? :default [a b]
  (= a b))
        
(defn pairs-when [f source-a source-b]
  (for [a source-a
        b source-b
        :when (f a b)]
    [a b]))                    

(defn props-filtered-on [f prop-set a b]
  (->> prop-set
       (map #(vector % (get a %) (get b %)))
       (filter #(f (second %) (nth % 2)))
       (map #(first %))))

(defmulti coll-diff (fn [a b] [(type a) (type b)]))

(defn changed-keys [keys a b]
  (loop [cur-keys keys
         acc {}]
    (if (empty? cur-keys)
      acc
      (let [cur-key (first cur-keys)]
        (recur (rest cur-keys) (assoc acc cur-key (coll-diff (get a cur-key) (get b cur-key))))))))

(defmethod coll-diff [clojure.lang.IPersistentVector clojure.lang.IPersistentVector] [a b]
  (let [common (pairs-when corresponds? a b)]
    (diff/create (into [] (difference (set b) (set (map second common))))
                 (into [] (difference (set a) (set (map first common))))
                 (map first (filter #(= (first %) (second %)) common))
                 (map #(coll-diff (first %) (second %)) (filter #(not= (first %) (second %)) common)))))

(defmethod coll-diff [clojure.lang.IPersistentMap clojure.lang.IPersistentMap] [a b]
  (let [keys-a (set (keys a))
        keys-b (set (keys b))
        common (intersection keys-a keys-b)]
    (diff/create (select-keys b (difference keys-b keys-a))
                 (select-keys a (difference keys-a keys-b))
                 (select-keys a (props-filtered-on = common a b))
                 (changed-keys (props-filtered-on not= common a b) a b))))

(defmethod coll-diff :default [a b]
  {"prev" a "cur" b})
