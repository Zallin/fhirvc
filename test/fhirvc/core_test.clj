(ns fhirvc.core-test
  (:require [clojure.test :refer :all]
            [fhirvc.core :refer :all]))

(deftest single-primitive-property-deleted
  (let [a {:a 3 :b 4}
        b {:b 4}]
    (is (= (coll-diff a b)
           {:removed {:a 3}
            :added {}
            :b 4}))))

(deftest multiple-primitive-properties-deleted
  (let [a {:a 3 :b 4}
        b {}]
    (is (= (coll-diff a b)
           {:removed {:a 3 :b 4}
            :added {}}))))

(deftest single-primitive-property-added
  (let [a {:a 3}
        b {:a 3 :b 4}]
    (is (= (coll-diff a b)
           {:added {:b 4}
            :removed {}
            :a 3}))))

(deftest multiple-primitive-properties-added
  (let [a {:a 3}
        b {:a 3 :b 4 :c 5}]
    (is (= (coll-diff a b)
           {:added {:b 4
                :c 5}
            :removed {}
            :a 3}))))

(deftest single-compound-property-deleted
  (let [a {:a 3 :b [{:c 6} 2 3]}
        b {:a 3}]
    (is (= (coll-diff a b)
           {:removed {:b [{:c 6} 2 3]}
            :added {}
            :a 3}))))

(deftest single-compound-property-added
  (let [a {:a 3}
        b {:a 3 :b {:c 1 :d 1}}]
    (is (= (coll-diff a b)
           {:added {:b {:c 1 :d 1}}
            :removed {}
            :a 3}))))

(deftest single-primitive-property-changed
  (let [a {:a 1}
        b {:a 2}]
    (is (= (coll-diff a b)
           {:removed {}
            :added {}
            :a {:removed 1 :added 2}}))))

(deftest single-primitive-property-changed-to-compound
  (let [a {:a 1}
        b {:a {:b 1}}]
    (is (= (coll-diff a b)
           {:removed {}
            :added {}
            :a {:removed 1 :added {:b 1}}}))))

(deftest single-compound-property-changed-to-primitive
  (let [a {:a {:b 1}}
        b {:a 1}]
    (is (= (coll-diff a b)
           {:removed {}
            :added {}
            :a {:removed {:b 1} :added 1}}))))


(deftest value-in-hashmap-property-changed
  (let [a {:a {:b 1}}
        b {:a {:b 2}}]
    (is (= (coll-diff a b)
           {:added {}
            :removed {}
            :a {:added {} :removed {} :b {:added 2 :removed 1}}}))))
                
(deftest value-in-hashmap-property-deleted
  (let [a {:a {:b 1 :c 2}}
        b {:a {:b 1}}]
    (is (= (coll-diff a b)
           {:added {}
            :removed {}
            :a {:removed {:c 2}
                :added {}
                :b 1}}))))

(deftest value-in-hashmap-property-added
  (let [a {:a {:b 1}}
        b {:a {:b 1 :c 2}}]
    (is (= (coll-diff a b)
           {:added {}
            :removed {}
            :a {:added {:c 2}
                :removed {}
                :b 1}}))))

(deftest added-element-in-array
  (let [a {:a [1 2 3]}
        b {:a [1 2 3 4]}]
    (is (= (coll-diff a b)
           {:added {}
            :removed {}
            :a [{:added [4] :removed []} 1 3 2]}))))
           
(deftest value-in-array-property-deleted
  (let [a {:a [1 2 3]}
        b {:a [1 2]}]
    (is (= (coll-diff a b)
           {:added {} :removed {} :a [{:added [] :removed [3]} 1 2]}))))

(deftest map-value-in-array-property-added
  (let [a {:a [{:a 1 :b 2}]}
        b {:a [{:a 1 :b 3}]}]
    (is (= (coll-diff a b)
           {:added {}
            :removed {}
            :a [{:added [{:a 1 :b 3}]
                 :removed [{:a 1 :b 2}]}]}))))
           
