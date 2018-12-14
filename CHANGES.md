## 0.5.2 (2018-12-14)

[API Docs](http://metosin.github.io/potpuri/0.5.2/index.html) - [compare](https://github.com/metosin/potpuri/compare/0.5.1...0.5.2)

- Add `where-fn` function to return the predicate fn used by `find-first` and other
similar functions

## 0.5.1 (7.11.2017)

[API Docs](http://metosin.github.io/potpuri/0.5.1/index.html) - [compare](https://github.com/metosin/potpuri/compare/0.5.0...0.5.1)

- Added `map-entries`,  `filter-entries` and `remove-entries`

## 0.5.0 (03.10.2017)

[API Docs](http://metosin.github.io/potpuri/0.5.0/index.html) - [compare](https://github.com/metosin/potpuri/compare/0.4.0...0.5.0)

- Added `remove-keys` and `remove-vals`
- Added `build-tree`

## 0.4.0 (30.11.2016)

[API Docs](http://metosin.github.io/potpuri/0.4.0/index.html) - [compare](https://github.com/metosin/potpuri/compare/0.3.0...0.4.0)

- Added `zip`

## 0.3.0 (26.4.2016)

[API Docs](http://metosin.github.io/potpuri/0.3.0/index.html) - [compare](https://github.com/metosin/potpuri/compare/0.2.3...0.3.0)

- Updated to use Cljc (drops Clojure 1.6 support)
- Added `index-by`
- Added `condas->`

## 0.2.3 (12.7.2015)

[API Docs](http://metosin.github.io/potpuri/0.2.3/index.html) - [compare](https://github.com/metosin/potpuri/compare/0.2.2...0.2.2)

- Added `if-all-let`

## 0.2.2 (7.4.2015)

[API Docs](http://metosin.github.io/potpuri/0.2.2/index.html) - [compare](https://github.com/metosin/potpuri/compare/0.2.1...0.2.2)

- `map-keys` and `map-vals` should now work with records
- Added `filter-keys` and `filter-vals`

## 0.2.1 (16.1.2015)

[API Docs](http://metosin.github.io/potpuri/0.2.1/index.html) - [compare](https://github.com/metosin/potpuri/compare/0.2.0...0.2.1)

- Added `assoc-first` and `update-first` functions which can be used to
update first matching element in collection
- Tests are now using `clojure.test` / `cljs.test` instead of Midje
- CI is running Cljs tests using Node
