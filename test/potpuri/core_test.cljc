(ns potpuri.core-test
  (:require #?(:clj [clojure.test :refer :all]
                    :cljs [cljs.test :refer-macros [deftest is testing]])
            [potpuri.core :as p]))

(deftest fn->test
  (let [inc-x  (p/fn-> :x inc)
        sum-doubled-vals (p/fn->> vals (map (partial * 2)) (apply +))
        m {:x 1 :y 2 :z 3}]
    (is (= (inc-x m) 2))
    (is (= (sum-doubled-vals m) 12))))

(deftest condas->test
  (is (= 3
         (p/condas-> 1 number
                     (= 1 number) (inc number)
                     (= 2 number) (inc number))))
  (is (= 2
         (p/condas-> 1 number
                     (= 1 number) (inc number)
                     (= 1 number) (inc number)))))

(deftest if-all-let
  (is (true? (p/if-all-let [a 1] true)))
  (is (true? (p/if-all-let [a 1 b 2] true)))
  (is (= :else (p/if-all-let [a nil b 2] :true :else)))
  (is (nil? (p/if-all-let [a 1 b nil] true))))

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
  (is (= (p/dissoc-in m [:b :c]) m))
  (is (= (p/dissoc-in {:a m} [:a :a]) {}))

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
  (is (= (p/assoc-if {:a 5} :a nil) {:a 5}))
  (is (= (p/assoc-if {} :a 1 :b false :c 2) {:a 1 :b false :c 2})))

(deftest where-fn-test
  (testing "fn?'s are returned as is"
    (let [f (constantly true)]
      (is (identical? f (p/where-fn f)))))

  (testing "ifn?'s are returned as is"
    (is (= :foo (p/where-fn :foo)))
    (is (= #{42} (p/where-fn #{42}))))

  (testing "map predicates"
    (let [p (p/where-fn {:foo 42})]
      (is (= (p {:foo 42}) true))
      (is (= (p {:foo 42
                 :bar 1337}) true))
      (is (= (p {:bar 1337}) false)))))

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

(deftest map-entries-test
  (is (= (p/map-entries (fn [[k v]] [k (inc v)]) {:a 1 :b 2}) {:a 2 :b 3})))

(deftest filter-keys-test
  (is (= {:a 1} (p/filter-keys #{:a} {:a 1 :b 2}))))

(deftest filter-vals-test
  (is (= {:a 1} (p/filter-vals #{1} {:a 1 :b 2}))))

(deftest filter-entries-test
  (is (= {:a 1} (p/filter-entries (comp #{1} second) {:a 1 :b 2}))))

(deftest remove-keys-test
  (is (= {:b 2} (p/remove-keys #{:a} {:a 1 :b 2}))))

(deftest remove-vals-test
  (is (= {:b 2} (p/remove-vals #{1} {:a 1 :b 2}))))

(deftest remove-entries-test
  (is (= {:b 2} (p/remove-entries (comp #{1} second) {:a 1 :b 2}))))

(deftest index-by-test
  (is (= {1 {:id 1 :v "foo"}
          2 {:id 2 :v "bar"}}
         (p/index-by :id [{:id 1 :v "foo"} {:id 2 :v "bar"}]))))

(deftest zip-test
  (is (= [[1 :a] [2 :b] [3 :c]] (p/zip [1 2 3] [:a :b :c]))))

(deftest build-tree-test
  (testing "Basic case, depth 1"
    (is (= [{:id 1
             :parent nil
             :children [{:id 2 :parent 1}
                        {:id 3 :parent 1}]}]
           (p/build-tree
             {:id-fn :id
              :parent-fn :parent
              :assoc-children-fn #(assoc %1 :children %2)}
             [{:id 1 :parent nil}
              {:id 2 :parent 1}
              {:id 3 :parent 1}]))))

  (testing "Basic case, modify items after building tree"
    (is (= [{:id 1
             :children [{:id 2}
                        {:id 3}]}]
           (p/build-tree
             {:id-fn :id
              :parent-fn :parent
              :item-fn #(dissoc % :parent)
              :assoc-children-fn #(assoc %1 :children %2)}
             [{:id 1 :parent nil}
              {:id 2 :parent 1}
              {:id 3 :parent 1}]))))

  (testing "Depth 2"
    (is (= [{:id 1
             :parent nil
             :children [{:id 2 :parent 1
                         :children [{:id 3 :parent 2}]}]}]
           (p/build-tree
             {:id-fn :id
              :parent-fn :parent
              :children-fn vec
              :assoc-children-fn #(assoc %1 :children %2)}
             [{:id 1 :parent nil}
              {:id 2 :parent 1}
              {:id 3 :parent 2}]))))

  (testing "Multiple roots"
    (is (= [{:id 1
             :parent nil
             :children [{:id 3 :parent 1}]}
            {:id 2
             :parent nil
             :children [{:id 4 :parent 2}]}]
           (p/build-tree
             {:id-fn :id
              :parent-fn :parent
              :assoc-children-fn #(assoc %1 :children %2)}
             [{:id 1 :parent nil}
              {:id 2 :parent nil}
              {:id 3 :parent 1}
              {:id 4 :parent 2}]))))

  (testing "Children as map"
    (is (= {1 {:id 1
               :parent nil
               :children {2 {:id 2 :parent 1}
                          3 {:id 3 :parent 1}}}}
           (p/build-tree
             {:id-fn :id
              :parent-fn :parent
              :children-fn #(p/index-by :id %)
              :assoc-children-fn #(assoc %1 :children %2)}
             [{:id 1 :parent nil}
              {:id 2 :parent 1}
              {:id 3 :parent 1}]))))

  (testing "Duplicate items"
    (is (= [{:id 1
             :parent nil
             :children [{:id 3 :parent 1}
                        {:id 4 :parent 1}]}
            {:id 2
             :parent nil
             :children [{:id 3 :parent 2}
                        {:id 4 :parent 2}]}]
           ;; In this case, user could separate :parents to items with :parent
           (p/build-tree
             {:id-fn :id
              :parent-fn :parent
              :assoc-children-fn #(assoc %1 :children %2)}
             (mapcat (fn [item]
                       (if (seq (:parents item))
                         (map #(-> item (dissoc :parents) (assoc :parent %)) (:parents item))
                         [(-> item (dissoc :parents) (assoc :parent nil))]))
                     [{:id 1 :parents nil}
                      {:id 2 :parents nil}
                      {:id 3 :parents [1 2]}
                      {:id 4 :parents [1 2]}])))))
  )
