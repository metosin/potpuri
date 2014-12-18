(defproject potpuri "0.1.0-SNAPSHOT"
  :description "Metosin potpuri"
  :url "https://github.com/metosin/potpuri"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "same as Clojure"}
  :dependencies [[org.clojure/clojure "1.6.0"]]
  :plugins [[codox "0.8.10"]]

  :cljx {:builds [{:rules :clj
                   :source-paths ["src"]
                   :output-path "target/generated/src"}
                  {:rules :cljs
                   :source-paths ["src"]
                   :output-path "target/generated/src"}]}
  :prep-tasks [["cljx" "once"]]
  :source-paths ["src" "target/generated/src"]

  :codox {:src-dir-uri "http://github.com/metosin/potpuri/blob/master/"
          :src-linenum-anchor-prefix "L"
          :src-uri-mapping {#"target/generated/src" #(str "src/" % "x")}}

  :profiles {:dev {:plugins [[lein-midje "3.1.3"]
                             [com.keminglabs/cljx "0.5.0"]]
                   :dependencies [[midje "1.6.3"]]}})
