(ns raml-clj-parser.yaml-test
  (:require [raml-clj-parser.yaml :as sut]
            [midje.sweet :as midje :refer [ fact throws ]])
  (:import [ org.yaml.snakeyaml.scanner ScannerException]))

(fact "should return error info when content is invalid YAML"
      (sut/load (slurp "test/resources/yaml/invalid/error.yaml") "test/resources/yaml/invalid/")
      => {::sut/error "Invalid YAML format"
          ::sut/reason  "mapping values are not allowed here\n in 'string', line 6, column 6:\n    ajdsf:\n         ^\n"})
