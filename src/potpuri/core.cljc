(ns potpuri.core
  #?(:cljs (:require-macros potpuri.core)))

(defmacro fn->
  "Creates a function that threads on input with `some->`"
  {:added "0.1.0"}
  [& body] `(fn [x#] (some-> x# ~@body)))

(defmacro fn->>
  "Creates a function that threads on input with `some->>`"
  {:added "0.1.0"}
  [& body] `(fn [x#] (some->> x# ~@body)))

;; https://blog.juxt.pro/posts/condas.html
(defmacro condas->
  "A mixture of cond-> and as-> allowing more flexibility in the test and step forms"
  {:added "0.3.0"}
  [expr name & clauses]
  (assert (even? (count clauses)))
  (let [pstep (fn [[test step]] `(if ~test ~step ~name))]
    `(let [~name ~expr
           ~@(interleave (repeat name) (map pstep (partition 2 clauses)))]
       ~name)))

(defmacro if-all-let
  "`bindings => [binding-form test, binding-form test ...]`

  If all tests are `true`, evaluates then with binding-forms bound to the values of
  tests, if not, yields `else.`"
  {:added "0.2.3"}
  ([bindings then] `(if-all-let ~bindings ~then nil))
  ([bindings then else]
   (reduce (fn [subform binding]
             `(if-let [~@binding] ~subform ~else))
           then (reverse (partition 2 bindings)))))

(defn path-vals
  "Returns vector of tuples containing path vector to the value and the value."
  {:added "0.1.0"}
  [m]
  (letfn
    [(pvals [l p m]
       (reduce
         (fn [l [k v]]
           (if (map? v)
             (pvals l (conj p k) v)
             (cons [(conj p k) v] l)))
         l m))]
    (pvals [] [] m)))

(defn assoc-in-path-vals
  "Re-created a map from it's path-vals extracted with (path-vals)."
  {:added "0.1.0"}
  [c] (reduce (partial apply assoc-in) {} c))

;; https://github.com/clojure/core.incubator/blob/master/src/main/clojure/clojure/core/incubator.clj
(defn dissoc-in
  "Dissociates an entry from a nested associative structure returning a new
  nested structure. `keys` is a sequence of keys. Any empty maps that result
  will not be present in the new structure."
  {:added "0.1.0"}
  [m [k & ks :as keys]]
  (if ks
    (if-let [nextmap (get m k)]
      (let [newmap (dissoc-in nextmap ks)]
        (if (seq newmap)
          (assoc m k newmap)
          (dissoc m k)))
      m)
    (dissoc m k)))

(defmacro map-of
  "Creates map with symbol names as keywords as keys and
   symbol values as values."
  {:added "0.1.0"}
  [& syms]
  `(zipmap ~(vec (map keyword syms)) ~(vec syms)))

(defn deep-merge
  "Recursively merges maps.

   If the first parameter is a keyword it tells the strategy to
   use when merging non-map collections. Options are

   - `:replace`, the default, the last value is used
   - `:into`, if the value in every map is a collection they are concatenated
     using into. Thus the type of (first) value is maintained.

   Examples:

       (deep-merge {:a {:c 2}} {:a {:b 1}}) => {:a {:b 1 :c 2}}
       (deep-merge :replace {:a [1]} {:a [2]}) => {:a [2]}
       (deep-merge :into {:a [1]} {:a [2]}) => {:a [1 2]}
       (deep-merge {:a 1} nil) => nil

   See also: [meta-merge](https://github.com/weavejester/meta-merge)."
  {:added "0.2.0"
   :arglists '([strategy & values] [values])}
  [& values]
  (let [[values strategy] (if (keyword? (first values))
                            [(rest values) (first values)]
                            [values :replace])]
    (cond
      (every? map? values)
      (apply merge-with (partial deep-merge strategy) values)

      (and (= strategy :into) (every? coll? values))
      (reduce into values)

      :else
      (last values))))

(defn wrap-into
  "Wrap non-collection values into given collection.
   Collections are only put into the collection (non-wrapped).

   Examples:

       (wrap-into [] :a) => [:a]
       (wrap-into [] [:a]) => [:a]
       (wrap-into #{} [:a]) => #{:a}"
  {:added "0.2.0"}
  [coll v]
  (into coll (if (coll? v)
               v
               [v])))

(defn assoc-if
  "Assoc key-value pairs with non-nil values into map."
  {:added "0.2.0"}
  ([m key val] (if-not (nil? val) (assoc m key val) m))
  ([m key val & kvs]
   (let [ret (assoc-if m key val)]
     (if kvs
       (if (next kvs)
         (recur ret (first kvs) (second kvs) (nnext kvs))
         (throw
           #?(:clj (IllegalArgumentException. "assoc expects even number of arguments after map/vector, found odd number")
              :cljs "assoc expects even number of arguments after map/vector, found odd number")))
       ret))))

(defn where-fn
  "Returns a predicate that accepts a value and performs a check based on `where` argument.

  If `where` is a map, returns a predicate that compares all key/value pairs of `where` to
  the key/values of the value given to the predicate, and returns truthy value if all
  pairs are found.

  If `where` is a function (either `fn?` or `ifn?`), returns `where`.

  For all other values of `where` returns a predicate that compares the argument of predicate
  against `where` using `clojure.core/=`."
  {:added "0.5.2"}
  [where]
  (cond
    ; fn? and map? first as map also implements IFn
    (map? where)
    (fn [v]
      (every? (fn [[where-k where-v]]
                (= (get v where-k) where-v))
              where))

    ; Keywords, sets, ...
    (ifn? where)
    where

    :default
    (fn [v]
      (= v where))))

(defn find-index
  "Find index of vector which matches the `where` parameter.

   If `where` parameter is:

   - a map, a predicate is created which compares every key/value pair of `where` value to
     collection value.
   - something which implements IFn, e.g. keywords and sets, is used as is
   - other values, compares the item using using `clojure.core/=`

   Examples:

       (find-index [1 2 3] even?) => 1
       (find-index [{:id 1} {:id 2}] {:id 2}) => 1
       (find-index [{:a 1} {:b 2}] :b) => 1
       (find-index [1 2 3] #{3}) => 2
       (find-index [1 2 3] 3) => 2
       (-> [1 2 3] (find-index odd?)) => 0"
  {:added "0.2.0"}
  [coll where]
  (let [pred (where-fn where)]
    (first (keep-indexed #(if (pred %2) %1) coll))))

(defn find-first
  "Find first value from collection which matches the `where` parameter.

   If `where` parameter is:

   - a map, a predicate is created which compares every key/value pair of `where` value to
     collection value.
   - something which implements IFn, e.g. keywords and sets, is used as is
   - other values, compares the item using using `clojure.core/=`

   Examples:

       (find-first [1 2 3] even?) => 2
       (find-first [{:id 1} {:id 2, :foo :bar}] {:id 2}) => {:id 2, :foo :bar}
       (find-first [{:a 1} {:b 2, :foo :bar}] :b) => {:b 2, :foo :bar}
       (find-first [1 2 3] #{3}) => 3
       (find-first [1 2 3] 3) => 3
       (-> [1 2 3] (find-first odd?)) => 1"
  {:added "0.2.0"}
  [coll where]
  (let [pred (where-fn where)]
    (some #(if (pred %) %) coll)))

(defn assoc-first
  "Finds the first element in collection matching `where` parameter and
   replaces that with `v.`

   Implementation depends on collection type."
  {:added "0.2.1"}
  [coll where v]
  (let [pred (where-fn where)]
    (cond
      (vector? coll) (assoc coll (find-index coll pred) v)
      :default (map (fn [x]
                      (if (pred x) v x))
                    coll))))

(defn update-first
  "Finds the first element in collection matching the `where` parameter
   and updates that using `f.` `f` is called with current value and
   rest of update-first params.

   Implementation depends on collection type."
  {:added "0.2.1"}
  [coll where f & args]
  (let [pred (where-fn where)]
    (cond
      (vector? coll) (apply update-in coll [(find-index coll pred)] f args)
      :default (map (fn [x]
                      (if (pred x) (apply f x args) x))
                    coll))))

(def ^:private conjv' (fnil conj []))

(defn conjv
  "Append an element to a collection. If collection is `nil`, creates vector
   instead of sequence. The appending might happen on different places
   depending on the type of collection.

   Examples:

       (conjv nil 5) => [5]
       (conjv [1] 2) => [1 2]
       (update-in {} [:a] conjv 5) => {:a [5]}
       (-> [] (conjv 5)) => [5]"
  {:added "0.2.0"}
  [coll el]
  (conjv' coll el))

(defn consv
  "Prepend an element to a collection. Returns a vector.

   Examples:

       (consv nil 1) => [1]
       (consv [2] 1) => [1 2]
       (update-in {:a 2} [:a] consv 1) => {:a [1 2]}
       (-> [2] (consv 5)) => [1 2]"
  {:added "0.2.0"}
  [coll el]
  (apply vector el coll))

;;;; map for kv collections

;; These are like ones in medley

(defn- editable? [coll]
  #?(:clj  (instance? clojure.lang.IEditableCollection coll)
     :cljs (satisfies? cljs.core.IEditableCollection coll)))

(defn- reduce-map [f coll]
  (if (editable? coll)
    (persistent! (reduce-kv (f assoc!) (transient (empty coll)) coll))
    (reduce-kv (f assoc) (empty coll) coll)))

(defn map-keys
  "Map the keys of given associative collection using function."
  {:added "0.2.0"}
  [f coll]
  (reduce-map (fn [xf] (fn [m k v]
                         (xf m (f k) v)))
              coll))

(defn map-vals
  "Map the values of given associative collection using function."
  {:added "0.2.0"}
  [f coll]
  (reduce-map (fn [xf] (fn [m k v]
                         (xf m k (f v))))
              coll))

(defn map-entries
  "Map the entries of given associative collection using function."
  {:added "0.5.1"}
  [f coll]
  (reduce-map (fn [xf] (fn [m k v]
                (let [[k v] (f [k v])]
                  (xf m k v))))
              coll))

(defn filter-keys
  "Filter given associative collection using function on the keys."
  {:added "0.2.2"}
  [pred coll]
  (reduce-map (fn [xf] (fn [m k v]
                         (if (pred k) (xf m k v) m)))
              coll))

(defn filter-vals
  "Filter given associative collection using function on the values."
  {:added "0.2.2"}
  [pred coll]
  (reduce-map (fn [xf] (fn [m k v]
                         (if (pred v) (xf m k v) m)))
              coll))

(defn filter-entries
  "Filter given associative collection using function on the values."
  {:added "0.5.1"}
  [pred coll]
  (reduce-map (fn [xf] (fn [m k v]
                (if (pred [k v]) (xf m k v) m)))
              coll))

(defn remove-keys
  "Removes given associative collection using function on the keys."
  {:added "0.5.0"}
  [pred coll]
  (filter-keys (complement pred) coll))

(defn remove-vals
  "Removes given associative collection using function on the values."
  {:added "0.5.0"}
  [pred coll]
  (filter-vals (complement pred) coll))

(defn remove-entries
  "Removes given associative collection using function on the values."
  {:added "0.5.1"}
  [pred coll]
  (filter-entries (complement pred) coll))

(defn index-by
  "Returns a map of the elements of coll keyed by the result of
  f on each element. The value at each key will the last item
  for given f result."
  {:added "0.3.0"}
  [f coll]
  ; FIXME: perf test against reduce+transient and zipmap
  (into {} (map (juxt f identity)) coll))

(defn zip
  "Returns a sequence of vectors where the i-th vector contains
  the i-th element from each of the argument collections. The returned
  sequence is as long as the shortest argument.

  Example:

      (zip [1 2 3] [:a :b :c])  => ([1 :a] [2 :b] [3 :c])
      (zip [1] [1 2] [1 2 3])   => ([1 1 1])"
  {:added "0.4.0"}
  [& colls]
  (apply map vector colls))

(defn- build-tree' [{:keys [id-fn item-fn children-fn assoc-children-fn]
                     :or {children-fn identity
                          item-fn identity}
                     :as opts}
                    g
                    items]
  (children-fn
    (map (fn [item]
           (let [children (build-tree' opts g (get g (id-fn item)))]
             (item-fn
               (if (seq children)
                 (assoc-children-fn item children)
                 item))))
         items)))

(defn build-tree
  "Builds a tree from given items collections.

  ID is what is used to match parents and children.
  Root items are those where parent-fn returns nil.

  Options:

  - :parent-fn (required) Used to create a map from ID => children
  - :id-fn (required) Used to get the ID from an item
  - :assoc-children-fn (required) Attach the children to an item
  - :item-fn (optional) Called for each item, after children has been attached to the item
  - :children-fn (optional) Called for each children collection

  Example:
    (build-tree {:id-fn :id, :parent-fn :parent, :assoc-children-fn #(assoc %1 :children %2)}
                [{:id 1} {:id 2 :parent 1} {:id 3 :parent 1}])
    => [{:id 1 :children [{:id 2} {:id 3}]}]

  Check test file for more examples."
  {:added "0.5.0"}
  [{:keys [parent-fn id-fn assoc-children-fn] :as opts} items]
  (assert parent-fn ":parent-fn option is required.")
  (assert id-fn ":id-fn option is required.")
  (assert assoc-children-fn ":assoc-children-fn option is required.")

  (let [g (group-by parent-fn items)]
    ;; Start with items which have no parent => root items
    (build-tree' opts g (get g nil))))
