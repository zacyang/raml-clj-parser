(ns raml-clj-parser.integration-test
  (:require [raml-clj-parser.core :as core]
            [midje.sweet :refer [fact =>]]))


(fact "should not blow up..."
      (let [result (core/read-raml "test/resources/raml/v08/sample.raml")]
        (:error result) => nil))
