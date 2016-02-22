(defproject metosin/potpuri "0.3.0-SNAPSHOT"
  :description "Metosin potpuri"
  :url "https://github.com/metosin/potpuri"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "same as Clojure"}
  :dependencies [[org.clojure/clojure "1.8.0"]]
  :plugins [[codox "0.8.13"]]

  :source-paths ["src" "target/generated/src"]
  :test-paths ["test" "target/generated/test"]

  :codox {:src-dir-uri "http://github.com/metosin/potpuri/blob/master/"
          :src-linenum-anchor-prefix "L"
          :defaults {:doc/format :markdown}}

  :profiles {:dev {:plugins [[jonase/eastwood "0.2.1"]]
                   :dependencies [[criterium "0.4.4"]
                                  [org.clojure/clojurescript "1.7.228"]]}
             :1.7 {:dependencies [[org.clojure/clojure "1.7.0"]]}}
  :aliases {"all" ["with-profile" "dev:dev,1.7"]
            "test-clj"  ["all" "do" ["test"] ["check"]]})
