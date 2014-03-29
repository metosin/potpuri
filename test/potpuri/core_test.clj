(ns potpuri.core-test
  (:require [midje.sweet :refer :all]
            [potpuri.core :refer :all]))

(fact "fn-> & fn->>"
  (let [inc-x  (fn-> :x inc)
        sum-doubled-vals (fn->> vals (map (partial * 2)) (apply +))
        m {:x 1 :y 2 :z 3}]
    (inc-x m) => 2
    (sum-doubled-vals m) => 12))
