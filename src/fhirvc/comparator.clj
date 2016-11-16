(ns fhirvc.comparator
  (:require [clojure.set :refer [intersection difference]]
            [cheshire.core :refer :all]
            [config.core :refer [env]]
            [fhirvc.diff :as diff]))

(defn contains-all? [obj & keys]
  (every? (partial contains? obj) keys))

(defn primary-attributes [obj]
  (cond (contains-all? obj "resourceType" "name") ["resourceType" "name"]
        (contains? obj "path") ["path"]
        (contains? obj "name") ["name"]
        :else nil))

(defn corresponds? [a b]
  (if (and (coll? a) (coll? b))
    (let [pr-attrs (primary-attributes a)]
      (if (nil? pr-attrs)
        (= a b)
        (every? #(= (get a %) (get b %)) pr-attrs)))
    (= a b)))

(defn pairs-when [f source-a source-b]
  (for [a source-a
        b source-b
        :when (f a b)]
    [a b]))                    

(defmulti coll-diff (fn [a b] [(type a) (type b)]))

(defmethod coll-diff [clojure.lang.IPersistentVector clojure.lang.IPersistentVector] [a b]
  (let [common (pairs-when corresponds? a b)]
    (diff/create (into [] (difference (set b) (set (map second common))))
                 (into [] (difference (set a) (set (map first common))))
                 (map #(coll-diff (first %) (second %)) (filter #(not= (first %) (second %)) common))
                 (map first (filter #(= (first %) (second %)) common)))))

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
    (diff/create (select-keys b (difference keys-b keys-a))
                 (select-keys a (difference keys-a keys-b))
                 (changed-keys (props-filtered-on not= common a b) a b)
                 (select-keys a (props-filtered-on = common a b)))))

(defmethod coll-diff :default [a b]
  {"prev" a "cur" b})
