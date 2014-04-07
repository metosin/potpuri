(ns potpuri.core-test
  (:require [midje.sweet :refer :all]
            [potpuri.core :refer :all]))

(facts "fn-> & fn->>"
  (let [inc-x  (fn-> :x inc)
        sum-doubled-vals (fn->> vals (map (partial * 2)) (apply +))
        m {:x 1 :y 2 :z 3}]
    (inc-x m) => 2
    (sum-doubled-vals m) => 12))

(fact "path-vals & assoc-in-path-vals"
  (let [original {:a {:b {:c 1
                          :d 2}
                      :e 3}}
        target [[[:a :e] 3]
                [[:a :b :d] 2]
                [[:a :b :c] 1]]]
    (path-vals original) => target
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
