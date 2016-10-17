(ns fhirvc.adt
  (:require [clojure.string :refer [replace]]))

;; comparison adt

(defn fhir-comparison [name-a name-b diff]
  [name-a name-b diff])

(defn fhir-diff [comparison]
  (get comparison "difference"))

(defn fhir-names [comparison]
  (let [names [(get comparison "version-a") (get comparison "version-b")]]
    (map #(replace % "_" " ") names)))

(defn comp-ref [comp]
  (let [[a b] (fhir-names comp)]
    (str (replace (str a " " b) " " "_") "_page.html")))

;; data structure difference adt

(defn added [diff]
  (get diff "added"))

(defn removed [diff]
  (get diff "removed"))

(defn changed [diff]
  (get diff "changed"))

(defn unchanged [diff]
  (get diff "unchanged"))

(defn diff-name [diff]
  (get-in diff ["unchanged" "name"]))                


;; definition abstraction?

(defn def-name [def]
  (if (not (nil? (get def "name")))
    (get def "name")
    (diff-name def)))

(defn def-type [def]
  (if (not (nil? (get def "resourceType")))
    (get def "resourceType")
    (get-in def ["unchanged" "resourceType"])))

(defn def-ref [def comp]
  (str (replace (comp-ref comp) "_page.html" "" ) "/" (def-name def) ".html"))
