(defproject metosin/potpuri "0.5.1"
  :description "Common stuff missing from the clojure.core."
  :url "https://github.com/metosin/potpuri"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "same as Clojure"}
  :dependencies [[org.clojure/clojure "1.8.0"]]
  :plugins [[lein-codox "0.10.3"]
            [metosin/boot-alt-test "0.4.0-20171019.180106-3"]]

  :alt-test {:report [:pretty
                      {:type :junit
                       :output-to "target/junit.xml"}]}

  :source-paths ["src" "target/generated/src"]
  :test-paths ["test" "target/generated/test"]

  :codox {:output-path "doc"
          :source-uri "https://github.com/metosin/potpuri/blob/{version}/{filepath}#L{line}"
          :metadata {:doc/format :markdown}}

  :profiles {:dev {:plugins [[jonase/eastwood "0.2.1"]]
                   :dependencies [[criterium "0.4.4"]
                                  [org.clojure/clojurescript "1.7.228"]]}
             :1.7 {:dependencies [[org.clojure/clojure "1.7.0"]]}}
  :deploy-repositories [["releases" :clojars]]
  :aliases {"all" ["with-profile" "dev:dev,1.7"]
            "test-clj"  ["all" "do" ["alt-test"] ["check"]]})
