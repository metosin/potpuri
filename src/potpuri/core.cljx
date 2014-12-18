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

; FIXME [coll where]
; where `where` is a map, fn or value?
(defn find-index
  "Find index of vector where item has matching value on given key.

   Usable with ->"
  {:added "0.2.0"}
  [coll k v]
  (first (keep-indexed #(if (= v (get %2 k)) %1) coll)))

; FIXME: [coll where]
(defn find-first
  "Find first value from collection where item has matching value for given key.

   Usable with ->"
  {:added "0.2.0"}
  [coll k v]
  (some (fn [x] (if (= v (get x k)) x)) coll))

(defn conjv
  "Append an element to a collection. Returns a vector.

   Usable with partial"
  {:added "0.2.0"}
  [el coll]
  (into [] (conj coll el)))

(defn consv
  "Prepend an element to a collection. Returns a vector.

   Usable with partial"
  {:added "0.2.0"}
  [el coll]
  (into [] (cons el coll)))
