(ns potpuri.core-test
  (:require [#+clj clojure.test #+cljs cljs.test :as test #+clj :refer #+cljs :refer-macros [deftest is testing]]
            [potpuri.core :as p :include-macros true]
            #+clj [criterium.core :refer [quick-bench]]))

(deftest fn->test
  (let [inc-x  (p/fn-> :x inc)
        sum-doubled-vals (p/fn->> vals (map (partial * 2)) (apply +))
        m {:x 1 :y 2 :z 3}]
    (is (= (inc-x m) 2))
    (is (= (sum-doubled-vals m) 12))))

(def original {:a {:b {:c 1
                       :d 2}
                   :e 3}})
(def target [[[:a :e] 3]
             [[:a :b :d] 2]
             [[:a :b :c] 1]])

(deftest path-vals-test
  (is (= (set (p/path-vals original)) (set target))))

(deftest assoc-in-path-vals-test
  (is (= (p/assoc-in-path-vals target) original)))

(def m  {:a {:b1 {:c1 "kikka"
                  :c2 "kakka"}
             :b2 "kukka"}})

(deftest dissoc-in-test
  (is (= (p/dissoc-in m [:a]) {}))

  (is (= (p/dissoc-in m [:a :b2])
         {:a {:b1 {:c1 "kikka"
                   :c2 "kakka"}}}))

  (is (= (p/dissoc-in m [:a :b1 :c2])
         {:a {:b1 {:c1 "kikka"}
              :b2 "kukka"}}))

  (is (= (p/dissoc-in m [nil]) m)))

(deftest map-of-test
  (let [a 1 b true c [:abba :jabba]]
    (is (= (p/map-of a b c)
           {:a 1 :b true :c [:abba :jabba]}))))

(deftest deep-merge-test
  (testing "basics"
    (is (= (p/deep-merge {:a {:c 2}} {:a {:b 1}}) {:a {:b 1 :c 2}}))
    (is (= (p/deep-merge {:a 1} {:a 2}) {:a 2}))
    (is (= (p/deep-merge {:a {:b 1}} {:a {:b 2}}) {:a {:b 2}}))
    (is (= (p/deep-merge {:a {:b 1}} {:a {:b nil}}) {:a {:b nil}}))
    (is (= (p/deep-merge {:a 1} nil) nil))
    )

  (testing "sequentials"
    (is (= (p/deep-merge {:a [1]} {:a [2]}) {:a [2]}))
    (is (= (p/deep-merge :into {:a [1]} {:a [2]}) {:a [1 2]}))
    (is (= (p/deep-merge :into {:a #{:a}} {:a #{:b}}) {:a #{:b :a}}))))

(deftest wrap-into-test
  (is (= (p/wrap-into [] "foo") ["foo"]))
  (is (= (p/wrap-into [] ["a" "b"]) ["a" "b"]))
  (is (= (p/wrap-into #{} "a") #{"a"}))
  (is (= (p/wrap-into #{} ["a" "b"]) #{"a" "b"})))

(deftest assoc-if-test
  (is (= (p/assoc-if {} :a 5) {:a 5}))
  (is (= (p/assoc-if {:a 5} :b nil) {:a 5}))
  (is (= (p/assoc-if {:a 5} :a nil) {:a 5})))

(deftest conjv-test
  (is (= (p/conjv [1 2] 3) [1 2 3]))
  (is (= (update-in {:a [1 2]} [:a] p/conjv 3) {:a [1 2 3]}))
  (is (= (-> [1 2] (p/conjv 3)) [1 2 3]))
  (testing "conjv to nil will create vec instead of seq"
    (is (vector? (:a (update-in {} [:a] p/conjv 1))))))

(deftest consv-test
  (is (= (p/consv [2 3] 1) [1 2 3]))
  (is (= (update-in {:a [2 3]} [:a] p/consv 1) {:a [1 2 3]}))
  (is (= (-> [2 3] (p/consv 1)) [1 2 3])))

(comment
  (quick-bench (into [1] [2 3 4]))
  (quick-bench (apply vector 1 [ 2 3 4])))

(def test-coll [{:id 1 :foo "bar"}
                {:id 2 :foo "foo"}])

(deftest find-index-test
  (testing "map based where"
    (is (= (p/find-index test-coll {:id 1}) 0))
    (is (= (p/find-index test-coll {:id 2}) 1))
    (is (= (p/find-index test-coll {:id 2 :foo "foo"}) 1)))

  (testing "predicate where"
    (is (= (p/find-index test-coll (comp even? :id)) 1)))

  (testing "keyword where"
    (is (= (p/find-index test-coll :id) 0)))

  (testing "set where"
    (is (= (p/find-index ["a" "b" "c"] #{"c"}) 2)))

  (testing "value identity where"
    (is (= (p/find-index [4 3 2] 3) 1)))

  (testing "-> syntax"
    (is (= (-> test-coll (p/find-index {:id 2})) 1)))

  (testing "different coll types"
    (testing "seq"
      (is (= (p/find-index (seq test-coll) {:id 1}) 0)))))

(deftest find-first-test
  (is (= (p/find-first test-coll {:id 2}) (nth test-coll 1)))
  (is (= (-> test-coll (p/find-first {:id 2})) (nth test-coll 1))))

(deftest assoc-first-test
  (is (= (p/assoc-first test-coll {:id 2} {:id 2 :foo "zzz"}) (assoc-in test-coll [1 :foo] "zzz")))
  (testing "seq"
    (is (= (p/assoc-first (seq test-coll) {:id 2} {:id 2 :foo "zzz"}) (seq (assoc-in test-coll [1 :foo] "zzz"))))))

(deftest update-first-test
  (is (= (p/update-first test-coll {:id 2} #(assoc % :foo "zzz")) (assoc-in test-coll [1 :foo] "zzz")))
  (testing "rest args"
    (is (= (p/update-first test-coll {:id 2} assoc :foo "zzz") (assoc-in test-coll [1 :foo] "zzz"))))
  (testing "seq"
    (is (= (p/update-first (seq test-coll) {:id 2} assoc :foo "zzz") (seq (assoc-in test-coll [1 :foo] "zzz"))))))

(deftest map-keys-test
  (is (= (p/map-keys keyword {"a" 1 "b" 2}) {:a 1 :b 2})))

(deftest map-vals-test
  (is (= (p/map-vals inc {:a 1 :b 2}) {:a 2 :b 3})))
