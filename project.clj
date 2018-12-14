(defproject metosin/potpuri "0.5.2-SNAPSHOT"
  :description "Common stuff missing from the clojure.core."
  :url "https://github.com/metosin/potpuri"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "same as Clojure"}
  :dependencies []
  :plugins [[lein-codox "0.10.3"]
            [metosin/bat-test "0.4.1"]]

  :bat-test {:report [:pretty
                      {:type :junit :output-to "target/junit.xml"}]}

  :source-paths ["src" "target/generated/src"]
  :test-paths ["test" "target/generated/test"]

  :codox {:output-path "doc"
          :source-uri "https://github.com/metosin/potpuri/blob/{version}/{filepath}#L{line}"
          :metadata {:doc/format :markdown}}

  :profiles {:dev {:plugins [[jonase/eastwood "0.2.1"]]
                   :dependencies [[org.clojure/clojure "1.9.0"]
                                  [criterium "0.4.4"]
                                  ;; FIXME: temp fix for JDK 11 - can be removed once
                                  ;; eftest/bat-test are updated.
                                  [mvxcvi/puget "1.0.3"]
                                  [org.clojure/clojurescript "1.10.439"]]}
             :1.7 {:dependencies [[org.clojure/clojure "1.7.0"]
                                  ;; Test aren't run with old cljs - but new cljs would bring in
                                  ;; new tools.reader which doesn't work with old Clojure.
                                  [org.clojure/clojurescript "1.7.228"]]}
             :1.8 {:dependencies [[org.clojure/clojure "1.8.0"]]}
             :1.10 {:dependencies  [[org.clojure/clojure "1.10.0-RC2"]]}}
  :deploy-repositories [["releases" :clojars]]
  :aliases {"all" ["with-profile" "dev:dev,1.7:dev,1.8:dev,1.10"]
            "test-clj"  ["all" "do" ["bat-test"] ["check"]]})
