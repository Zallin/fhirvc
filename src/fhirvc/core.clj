(ns fhirvc.core
  (:require [clojure.set :refer [intersection difference]]
            [cheshire.core :refer :all]
            [config.core :refer [env]]
            [fhirvc.utils :refer :all]))

(defn fhir-files [version]
  (->> (:fhir-versions env)
       (filter #(= version (:name %)))
       first
       :files
       (map (fn [filepath]
              (let [fname (filename filepath)]
                {:filename fname
                 :content (slurp filepath)})))))

(defn corresponds? [file-a file-b]
  (= (:filename file-a) (:filename file-b)))

(defn filepairs [version-a version-b]
  (for [file-a (fhir-files version-a)
        file-b (fhir-files version-b)
        :when (corresponds? file-a file-b)]
    {:filenames [(:filename file-a) (:filename file-b)]
     :content-a (:content file-a)
     :content-b (:content file-b)}))    

(defmulti coll-diff (fn [a b] [(type a) (type b)]))

(defmethod coll-diff [clojure.lang.PersistentArrayMap clojure.lang.PersistentArrayMap] [a b]
  (let [keys-a (set (keys a))
        keys-b (set (keys b))
        removed {:removed (select-keys a (difference keys-a keys-b))}
        added {:added (select-keys b (difference keys-b keys-a))}]
    (loop [keys (intersection keys-a keys-b)
           acc-hm (merge removed added)]
      (if (empty? keys)
        acc-hm
        (let [cur-key (first keys)
              val-a (get a cur-key)
              val-b (get b cur-key)]
          (cond (= val-a val-b) (recur (rest keys) (assoc acc-hm cur-key val-a))
                :else (recur (rest keys) (assoc acc-hm cur-key (coll-diff val-a val-b)))))))))

(defmethod coll-diff [clojure.lang.PersistentVector clojure.lang.PersistentVector] [a b]
  (let [freq-a (frequencies a)
        freq-b (frequencies b)
        only-in-first (fn [a b]
                        (mapcat
                         (fn [k] (repeat (get a k) k))
                         (difference (set (keys a)) (set (keys b)))))]                     
    (loop [keys (intersection (set (keys freq-a)) (set (keys freq-b)))
           elements-in-common []
           elements-in-a (only-in-first freq-a freq-b)
           elements-in-b (only-in-first freq-b freq-a)]
      (if (empty? keys)
        (conj elements-in-common {:removed elements-in-a :added elements-in-b})
        (let [cur-key (first keys)
              occ-in-a (get freq-a cur-key)
              occ-in-b (get freq-b cur-key)]
          (cond (= occ-in-a occ-in-b) (recur (rest keys)
                                             (concat elements-in-common
                                                     (repeat occ-in-a cur-key))
                                             elements-in-a
                                             elements-in-b)                                                                                   
                (< occ-in-a occ-in-b) (recur (rest keys)
                                             (concat elements-in-common
                                                     (repeat occ-in-a cur-key))
                                             elements-in-a
                                             (concat elements-in-b
                                                     (repeat  (- occ-in-b
                                                                 occ-in-a)
                                                            cur-key)))
                :else (recur (rest keys)
                             (concat elements-in-common
                                     (repeat occ-in-b cur-key))
                             (concat elements-in-a
                                     (repeat (- occ-in-a
                                                occ-in-b)
                                           cur-key))
                             elements-in-b)))))))                                                                   
  
(defmethod coll-diff :default [a b] {:removed a :added b})
                                                     
(defn contents-to-repr [file-hm]
  (assoc file-hm
         :repr-a (parse-string (:content-a file-hm))
         :repr-b (parse-string (:content-b file-hm))))

(defn contents-difference [file-hm]
   (assoc file-hm :difference (coll-diff (:repr-a file-hm)
                                         (:repr-b file-hm))))
  
(defn versions-diff [version-a version-b]
  (->> (filepairs version-a version-b)
       (map (fn [filepair]
              (-> filepair
                  contents-to-repr
                  contents-difference
                  (select-keys [:filenames :difference]))))
       generate-string))

(defn get-version-names []
  (map :name (:fhir-versions env)))
