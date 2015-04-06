(require 'cljs.closure)

(cljs.closure/build
  ; Includes :source-paths and :test-paths already
  "test"
  {:output-to "target/generated/js/out/tests.js"
   :main "potpuri.runner"
   :source-map "target/generated/js/out/tests.map.js"
   :output-dir "target/generated/js/out"
   :optimizations :none
   :target :nodejs})

(shutdown-agents)
