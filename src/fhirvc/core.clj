(ns fhirvc.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [config.core :refer [env]]
            [cheshire.core :refer :all]
            [clojure.java.io :refer [file]]
            [fhirvc.comparator :refer [coll-diff]]
            [fhirvc.generator :refer [generate-site]]
            [fhirvc.comp :as comp]
            [clojure.string :refer [trim]]))

(def cli-options
  [["-o", "--output DIR", "Output folder"
    :parse-fn #(trim %)
    :default "dist/site"]])

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

(defn update-seq [attr f seq]
  (map #(update % attr f) seq))

(defn to-edn [pair]
  (update-seq :data #(map parse-string %) pair))

(defn defs [pair]
  (update-seq :data #(into [] (map (fn [el] (get el "resource")) %))
              (update-seq :data #(mapcat (fn [cnt] (get cnt "entry")) %) pair)))
                      
(defn comparison [[a b]]
  (let [diff (coll-diff (:data b) (:data a))]
    (comp/create (:name a) (:name b) diff)))  

(defn -main [& args]
  (let [{options :options} (parse-opts args cli-options)]
    (generate-site (:output options)
                   (map (fn [pair]
                          (->> (files pair)
                               to-edn
                               defs
                               comparison))
                        (pairs (:versions env))))))
