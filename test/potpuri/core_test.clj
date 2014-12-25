(ns potpuri.core-test
  (:require [midje.sweet :refer :all]
            [potpuri.core :refer :all]))

(facts "fn-> & fn->>"
  (let [inc-x  (fn-> :x inc)
        sum-doubled-vals (fn->> vals (map (partial * 2)) (apply +))
        m {:x 1 :y 2 :z 3}]
    (inc-x m) => 2
    (sum-doubled-vals m) => 12))

(let [original {:a {:b {:c 1
                        :d 2}
                    :e 3}}
      target [[[:a :e] 3]
              [[:a :b :d] 2]
              [[:a :b :c] 1]]]
  (fact path-vals
    (path-vals original) => (contains target :in-any-order))
  (fact assoc-in-path-vals
    (assoc-in-path-vals target) => original))

(fact "dissoc-in"
  (let [m {:a {:b1 {:c1 "kikka"
                    :c2 "kakka"}
               :b2 "kukka"}}]
    (dissoc-in m [:a]) => {}
    (dissoc-in m [:a :b2]) => {:a {:b1 {:c1 "kikka"
                                        :c2 "kakka"}}}
    (dissoc-in m [:a :b1 :c2]) => {:a {:b1 {:c1 "kikka"}
                                       :b2 "kukka"}}
    (dissoc-in m [nil]) => m))

(fact "map-of"
  (let [a 1 b true c [:abba :jabba]]
    (map-of a b c) => {:a 1 :b true :c [:abba :jabba]}))

(facts deep-merge
  (deep-merge {:a {:c 2}} {:a {:b 1}}) => {:a {:b 1 :c 2}}
  (deep-merge {:a 1} {:a 2}) => {:a 2}
  (deep-merge {:a {:b 1}} {:a {:b 2}}) => {:a {:b 2}}
  (deep-merge {:a {:b 1}} {:a {:b nil}}) => {:a {:b nil}}
  (deep-merge {:a 1} nil) => nil)

(facts wrap-into
  (wrap-into [] "foo") => ["foo"]
  (wrap-into [] ["a" "b"]) => ["a" "b"]
  (wrap-into #{} "a") => #{"a"}
  (wrap-into #{} ["a" "b"]) => #{"a" "b"})

(facts assoc-if
  (assoc-if {} :a 5) => {:a 5}
  (assoc-if {:a 5} :b nil) => {:a 5}
  (assoc-if {:a 5} :a nil) => {:a 5})

(facts conjv
  (conjv [1 2] 3) => [1 2 3]
  (update-in {:a [1 2]} [:a] conjv 3) => {:a [1 2 3]}
  (-> [1 2] (conjv 3)) => [1 2 3]
  (fact "conjv to nil will create vec instead of seq"
    (:a (update-in {} [:a] conjv 1)) => vector?))

(facts consv
  (consv [2 3] 1) => [1 2 3]
  (update-in {:a [2 3]} [:a] consv 1) => {:a [1 2 3]}
  (-> [2 3] (consv 1)) => [1 2 3])

(def test-coll [{:id 1 :foo "bar"}
                {:id 2 :foo "foo"}])

(facts find-index
  (fact "map based where"
    (find-index test-coll {:id 1}) => 0
    (find-index test-coll {:id 2}) => 1
    (find-index test-coll {:id 2 :foo "foo"}) => 1)

  (fact "predicate where"
    (find-index test-coll (comp even? :id)) => 1)
  (fact "value identity where"
    (find-index [4 3 2] 3) => 1)

  (-> test-coll (find-index {:id 2})) => 1)

(facts find-first
  (find-first test-coll {:id 2}) => (nth test-coll 1)
  (-> test-coll (find-first {:id 2})) => (nth test-coll 1))
