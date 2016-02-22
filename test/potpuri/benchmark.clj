(ns potpuri.benchmark
  (:require [potpuri.core :as p]
            [criterium.core :as criterium]))

(comment
  (quick-bench (into [1] [2 3 4]))
  (quick-bench (apply vector 1 [ 2 3 4])))
