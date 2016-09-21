(ns fhirvc.core-test
  (:require [clojure.test :refer :all]
            [fhirvc.core :refer :all]))

(deftest single-primitive-property-deleted
  (let [a {:a 3 :b 4}
        b {:b 4}]
    (is (= (hashmaps-diff a b)
           {:- {:a 3}
            :+ {}
            :b 4}))))

(deftest multiple-primitive-properties-deleted
  (let [a {:a 3 :b 4}
        b {}]
    (is (= (hashmaps-diff a b)
           {:- {:a 3 :b 4}
            :+ {}}))))

(deftest single-primitive-property-added
  (let [a {:a 3}
        b {:a 3 :b 4}]
    (is (= (hashmaps-diff a b)
           {:+ {:b 4}
            :- {}
            :a 3}))))

(deftest multiple-primitive-properties-added
  (let [a {:a 3}
        b {:a 3 :b 4 :c 5}]
    (is (= (hashmaps-diff a b)
           {:+ {:b 4
                :c 5}
            :- {}
            :a 3}))))

(deftest single-compound-property-deleted
  (let [a {:a 3 :b [{:c 6} 2 3]}
        b {:a 3}]
    (is (= (hashmaps-diff a b)
           {:- {:b [{:c 6} 2 3]}
            :+ {}
            :a 3}))))

(deftest single-compound-property-added
  (let [a {:a 3}
        b {:a 3 :b {:c 1 :d 1}}]
    (is (= (hashmaps-diff a b)
           {:+ {:b {:c 1 :d 1}}
            :- {}
            :a 3}))))

(deftest single-primitive-property-changed
  (let [a {:a 1}
        b {:a 2}]
    (is (= (hashmaps-diff a b)
           {:- {:a 1}
            :+ {:a 2}}))))

(deftest single-primitive-property-changed-to-compound
  (let [a {:a 1}
        b {:a {:b 1}}]
    (is (= (hashmaps-diff a b)
           {:- {:a 1}
            :+ {:a {:b 1}}}))))

(deftest single-compound-property-changed-to-primitive
  (let [a {:a {:b 1}}
        b {:a 1}]
    (is (= (hashmaps-diff a b)
           {:- {:a {:b 1}}
            :+ {:a 1}}))))

(deftest value-in-array-property-deleted
  (let [a {:a [1 2 3]}
        b {:a [1 2]}]
    (is (= (hashmaps-diff a b)
           {:- {:a [1 2 3]}
            :+ {:a [1 2]}}))))

(deftest value-in-array-property-added
  (let [a {:a [1 2]}
        b {:a [1 2 3]}]
    (is (= (hashmaps-diff a b)
           {:- {:a [1 2]}
            :+ {:a [1 2 3]}}))))

(deftest value-in-hashmap-property-changed
  (let [a {:a {:b 1}}
        b {:a {:b 2}}]
    (is (= (hashmaps-diff a b)
           {:+ {}
            :- {}
            :a {:- {:b 1}
                :+ {:b 2}}}))))

(deftest value-in-hashmap-property-deleted
  (let [a {:a {:b 1 :c 2}}
        b {:a {:b 1}}]
    (is (= (hashmaps-diff a b)
           {:+ {}
            :- {}
            :a {:- {:c 2}
                :+ {}
                :b 1}}))))

(deftest value-in-hashmap-property-added
  (let [a {:a {:b 1}}
        b {:a {:b 1 :c 2}}]
    (is (= (hashmaps-diff a b)
           {:+ {}
            :- {}
            :a {:+ {:c 2}
                :- {}
                :b 1}}))))


               
        

