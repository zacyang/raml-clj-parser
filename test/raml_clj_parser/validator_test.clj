(ns raml-clj-parser.validator-test
  (:require [raml-clj-parser.validator :as sut]
            [midje.sweet :as midje :refer [facts fact =>]]))

(facts "util fn test"
       (fact "should return true when no extra keys"
             (#'sut/no-extra-keys? {}) => true)

       (fact "should return false when contains extra keys"
             (#'sut/no-extra-keys? {:exter-key "value"}) => false))

(facts "Required properties of every node in RAML model must be provided with values."
       (fact "All required root elements should presents"
             (let [valid_root_level_value {}])
             ;(sut/is-all-root-presents? valid_root_level_value) => true
             )

       (fact "API title must presents and contains value")

       (fact "base uri must presents")

       (fact "if base uri contains reserve uri parameter version , we should parse it")

       (fact "uri parameter for baseuri other than version
https://github.com/raml-org/raml-spec/blob/master/versions/raml-08/raml-08.md#uri-parameters
")

       )
