(ns potpuri.core-test
  (:require [midje.sweet :refer :all]
            [potpuri.core :refer :all]))

(fact
  (+ 1 1) => 2)
