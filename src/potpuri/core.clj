(ns potpuri.core)

(defmacro fn->
  "Creates a function that threads on input with some->"
  [& body] `(fn [x#] (some-> x# ~@body)))

(defmacro fn->>
  "Creates a function that threads on input with some->>"
  [& body] `(fn [x#] (some->> x# ~@body)))

(defn path-vals
  "Returns vector of tuples containing path vector to the value and the value."
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
  [c] (reduce (partial apply assoc-in) {} c))

;; https://github.com/clojure/core.incubator/blob/master/src/main/clojure/clojure/core/incubator.clj
(defn dissoc-in
  "Dissociates an entry from a nested associative structure returning a new
  nested structure. keys is a sequence of keys. Any empty maps that result
  will not be present in the new structure."
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
  [& syms]
  `(zipmap ~(vec (map keyword syms)) ~(vec syms)))

