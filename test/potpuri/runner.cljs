(ns potpuri.runner
  (:require [cljs.test :as test]
            [cljs.nodejs :as nodejs]
            potpuri.core-test))

(nodejs/enable-util-print!)

(defn -main []
  (test/run-all-tests #"^potpuri.*-test$"))

(set! *main-cli-fn* -main)
