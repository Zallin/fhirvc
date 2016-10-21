(ns fhirvc.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [config.core :refer [env]]
            [cheshire.core :refer :all]
            [clojure.java.io :refer [make-parents file]]
            [fhirvc.comparator :refer [coll-diff]]
            [fhirvc.generator :refer [generate-site]]
            [clojure.string :refer [trim]]))

(def cli-options
  [["-o", "--output DIR", "Output folder"
    :parse-fn #(trim %)
    :default "site"]])

(defn pairs [versions]
  (for [a versions
        b versions
        :while (not= a b)]
    [a b]))

;; TODO
;; filter directories
;; read files with provided extensions
(defn readdir [dir]
  (map slurp (rest (file-seq (file dir)))))

(defn files [[a b]]
  (let [data-a (readdir (str (:fhir-folder env) "/" a))
        data-b (readdir (str (:fhir-folder env) "/" b))]
    [{:name a
      :data data-a}
     {:name b
      :data data-b}]))

;; move to utils?
(defn update-seq [attr f seq]
  (map #(update % attr f) seq))

(defn parse-files [pair]
  (update-seq :data #(map parse-string %) pair))

(defn extract-defs [pair]
  (update-seq :data #(into [] (map (fn [el] (get el "resource")) %))
              (update-seq :data #(mapcat (fn [cnt] (get cnt "entry")) %) pair)))
                      
(defn write-to [dest diff-map]
  (let [filename (str dest "/" (:version-a diff-map) "_" (:version-b diff-map) ".json")]
    (make-parents filename)
    (spit filename (generate-string diff-map))))

(defn defs-difference [[a b]]
  (let [diff (coll-diff (:data b)
                         (:data a))]
    {"version-a" (:name a)
     "version-b" (:name b)
     "difference" diff}))

(defn -main [& args]
  (let [{options :options} (parse-opts args cli-options)]
    (generate-site (:output options)
                   (map (fn [pair]
                          (->> (files pair)
                               parse-files
                               extract-defs
                               defs-difference))
                        (pairs (:versions env))))))
