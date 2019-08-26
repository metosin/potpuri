(ns potpuri.benchmark
  (:require [potpuri.core :as p]
            [criterium.core :refer [quick-bench]]))

(comment
  (quick-bench (into [1] [2 3 4]))
  (quick-bench (apply vector 1 [ 2 3 4])))


(comment
  (def tree [{:id "top"
              :lapset [{:id "c1"
                        :lapset [{:id "c11" :lapset [{:id "c12"}]}
                                 {:id "c13"}]}
                       {:id "c2"}]}])

  (quick-bench (p/leafs tree :lapset)))
