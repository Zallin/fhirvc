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

(defn property-changed [key val-a val-b hm]
  (update (update hm :- (fn [removed]
                          (assoc removed key val-a)))
          :+
          (fn [added]
            (assoc added key val-b))))


(defn hashmaps-diff [a b]
  (let [keys-a (set (keys a))
        keys-b (set (keys b))
        removed {:- (select-keys a (difference keys-a keys-b))}
        added {:+ (select-keys b (difference keys-b keys-a))}]
    (loop [keys (intersection keys-a keys-b)
           acc-hm (merge removed added)]
      (if (empty? keys)
        acc-hm
        (let [cur-key (first keys)
              val-a (get a cur-key)
              val-b (get b cur-key)]
          (cond (= val-a val-b) (recur (rest keys) (assoc acc-hm cur-key val-a))
                (and (map? val-a) (map? val-b)) (recur (rest keys)
                                                       (assoc acc-hm cur-key (hashmaps-diff val-a val-b)))                                                               
                :else (recur (rest keys)
                             (property-changed cur-key val-a val-b acc-hm))))))))             

(defn contents-to-hashmap [file-hm]
  (assoc file-hm
         :hash-a (parse-string (:content-a file-hm))
         :hash-b (parse-string (:content-b file-hm))))

(defn contents-difference [file-hm]
   (assoc file-hm :difference (hashmaps-diff (:hash-a file-hm)
                                             (:hash-b file-hm))))
  
(defn versions-diff [version-a version-b]
  (->> (filepairs version-a version-b)
       (map (fn [filepair]
              (-> filepair
                  contents-to-hashmap
                  contents-difference
                  (select-keys [:filenames :difference]))))
       generate-string))

(defn get-version-names []
  (map :name (:fhir-versions env)))

