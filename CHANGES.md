## Unreleased

- Updated to use Cljc (drops Clojure 1.6 support)
- Added `index-by`

## 0.2.3 (12.7.2015)

- Added `if-all-let`

## 0.2.2 (7.4.2015)

- `map-keys` and `map-vals` should now work with records
- Added `filter-keys` and `filter-vals`

## 0.2.1 (16.1.2015)

- Added `assoc-first` and `update-first` functions which can be used to
update first matching element in collection
- Tests are now using `clojure.test` / `cljs.test` instead of Midje
- CI is running Cljs tests using Node
