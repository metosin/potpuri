(ns potpuri.core
  #+cljs (:require-macros potpuri.core))

(defmacro fn->
  "Creates a function that threads on input with some->"
  {:added "0.1.0"}
  [& body] `(fn [x#] (some-> x# ~@body)))

(defmacro fn->>
  "Creates a function that threads on input with some->>"
  {:added "0.1.0"}
  [& body] `(fn [x#] (some->> x# ~@body)))

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
  nested structure. keys is a sequence of keys. Any empty maps that result
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
  "creates map with symbol names as keywords as keys and
   symbol values as values."
  {:added "0.1.0"}
  [& syms]
  `(zipmap ~(vec (map keyword syms)) ~(vec syms)))

(defn deep-merge
  "Recursively merges maps.

   If the first parameter is a keyword it tells the strategy to
   use when merging non-map collections. Options are
   - :replace, the default, the last value is used
   - :into, if the value in every map is a collection they are concatenated
     using into. Thus the type of (first) value is maintained."
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
  "Assoc key-values pairs with non-nil values into map."
  {:added "0.2.0"}
  ([m key val] (if-not (nil? val) (assoc m key val) m))
  ([m key val & kvs]
   (let [ret (assoc-if m key val)]
     (if kvs
       (if (next kvs)
         (recur ret (first kvs) (second kvs) (nnext kvs))
         (throw (#+clj IllegalArgumentException. #+cljs str
                  "assoc expects even number of arguments after map/vector, found odd number")))
       ret))))

(defn- create-predicate [where]
  (cond
    (fn? where)
    where

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
  "Find index of vector which matches the where parameter.

   If where parameter is:
   - a fn, it's used as predicate as is
   - a map, a predicate is created which checks if value in collection has
     same values for each key in where map
   - Something which implements IFn, e.g. keywords and sets, is used as is
   - any value, a predicate is created which checks if value is identitical

   Usable with ->"
  {:added "0.2.0"}
  [coll where]
  (let [pred (create-predicate where)]
    (first (keep-indexed #(if (pred %2) %1) coll))))

(defn find-first
  "Find first value from collection which mathes the where parameter.

   Check find-index for documentation on where parameter.

   Usable with ->"
  {:added "0.2.0"}
  [coll where]
  (let [pred (create-predicate where)]
    (some #(if (pred %) %) coll)))

(defn assoc-first
  "Finds the first element in collection matching where parameter and
   replaces that with v.

   Implementation depends on collection type."
  {:added "0.2.1"}
  [coll where v]
  (let [pred (create-predicate where)]
    (cond
      (vector? coll) (assoc coll (find-index coll pred) v)
      :default (map (fn [x]
                      (if (pred x) v x))
                    coll))))

(defn update-first
  "Finds the first element in collection matchin where parameter
   and updates that using f. F is called with current value and
   rest of update-first params.

   Implementation depends on collection type."
  {:added "0.2.1"}
  [coll where f & args]
  (let [pred (create-predicate where)]
    (cond
      (vector? coll) (apply update-in coll [(find-index coll pred)] f args)
      :default (map (fn [x]
                      (if (pred x) (apply f x args) x))
                    coll))))

(defn conjv
  "Append an element to a collection. If collection is nil,
   creates vector instead of sequence.

   Usable with update-in, ->"
  {:added "0.2.0"}
  [coll el]
  ((fnil conj []) coll el))

(defn consv
  "Prepend an element to a collection. Returns a vector.

   Usable with update-in, ->"
  {:added "0.2.0"}
  [coll el]
  (apply vector el coll))

;;;; map for kv collections

;; These are like ones in medley

(defn- editable? [coll]
  #+clj (instance? clojure.lang.IEditableCollection coll)
  #+cljs (satisfies? cljs.core.IEditableCollection coll))

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

(defn filter-keys
  {:added "0.2.2"}
  [pred coll]
  (reduce-map (fn [xf] (fn [m k v]
                         (if (pred k) (xf m k v) m)))
              coll))

(defn filter-vals
  {:added "0.2.2"}
  [pred coll]
  (reduce-map (fn [xf] (fn [m k v]
                         (if (pred v) (xf m k v) m)))
              coll))
