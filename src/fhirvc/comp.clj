(ns fhirvc.comp
  (:require [clojure.string :as str]
            [fhirvc.diff :as diff]))

(defn create [name-a name-b diff]
  {"version-a" name-a
   "version-b" name-b
   "difference" diff})

(defn diff [comparison]
  (get comparison "difference"))

(defn first-name [comparison]
  (get comparison "version-a"))

(defn second-name [comparison]
  (get comparison "version-b"))

(defn names [comparison]
  (let [names [(first-name comparison) (second-name comparison)]]
    (map #(str/replace % "_" " ") names)))

(defn ref [comp]
  (let [a (first-name comp)
        b (second-name comp)]        
    (str a "_" b  "_page.html")))

(defn folder [comp]
  (str/replace (ref comp) "_page.html" ""))

(defn diff-ref [comp difference]
  (str (folder comp) "/" (diff/name difference) ".html"))
