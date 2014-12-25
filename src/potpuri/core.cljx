(ns potpuri.core)

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
  "Recursively merges maps if all the vals are maps."
  {:added "0.2.0"}
  [& vals]
  (if (every? map? vals)
    (apply merge-with deep-merge vals)
    (last vals)))

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
         (throw (IllegalArgumentException.
                  "assoc expects even number of arguments after map/vector, found odd number")))
       ret))))

(defn- create-predicate [where]
  (cond
    (fn? where)
    where

    (and (map? where) (= (count where) 1))
    (let [[where-k where-v] (first where)]
      (fn [v]
        (= (get v where-k) where-v)))

    (map? where)
    (fn [v]
      (not (some (fn [[where-k where-v]]
                   (not= (get v where-k) where-v))
                 where)))

    :default
    (fn [v]
      (= v where))))

(defn find-index
  "Find index of vector which matches the where parameter.

   If where parameter is:
   - a fn, it's used as predicate as is
   - a map, a predicate is created which checks if value in collection has
     same values for each key in where map
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
  (into [] (cons el coll)))
