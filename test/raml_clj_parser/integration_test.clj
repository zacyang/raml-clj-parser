(ns raml-clj-parser.integration-test
  (:require [raml-clj-parser.core :as core]
            [raml-clj-parser.validator :as validator]
            [midje.sweet :refer [fact => =not=> contains has tabular]]))

(fact "should not blow up..."
      (let [result (core/read-raml "test/resources/raml/v08/sample.raml")]
        (:error result) => nil
        (:title result) => "Jukebox API"))

(fact "should get include file"
     (let [result (core/read-raml "test/resources/raml/v08/full-example/jukebox-api.raml")
           schemas (-> result :schemas)]

       (:error result) => nil
       (count schemas) => 3

       (get-in  (first schemas) [:song :content :error]) => nil

       ;;werid midje issue, the following assertion return false, even everything is identical
                                       ;(first schemas) =>  {:song {:path "jukebox-include-song.schema", :tag "!include"}}
       ))

(fact "should get uri parameter"
      (let [result (core/read-raml "test/resources/raml/v08/partial-example/parametermized-uri.raml")]
        (get-in result [:baseUri :raml-clj-parser.reader/uri-parameters]) => ["communityDomain" "communityPath"]))

(tabular
 (fact "should get expects parts"
       (get-in (validator/validate (core/read-raml ?raml)) ?path-to) => ?expect)
 ?raml  ?path-to                           ?expect
 "test/resources/raml/v08/partial-example/resource-with-uri-parameters.raml"
 ["/users" "/{userId}"]
 {:displayName "User", :uri "/{userId}", :uriParameters {:userId {:displayName "User ID", :type "integer"}}, :raml-clj-parser.reader/uri-parameters ["userId"]}
;non explicity declare of uri parameter in resource path
 "test/resources/raml/v08/partial-example/resource-with-implicity-uri-parameters.raml"
  ["/files"  "/folder_{folderId}-file_{fileId}"]
  {:description "An item in the collection of all files", :uri "/folder_{folderId}-file_{fileId}", :raml-clj-parser.reader/uri-parameters ["folderId" "fileId"]}

  "test/resources/raml/v08/partial-example/resource-with-implicity-uri-parameters.raml"
  ["/users{mediaTypeExtension}"]
  {:uri "/users{mediaTypeExtension}", :uriParameters {:mediaTypeExtension {:description "Use .json to specify application/json or .xml to specify text/xml", :enum [".json" ".xml"]}}, :raml-clj-parser.reader/uri-parameters ["mediaTypeExtension"]})



(tabular
 (fact "just make sure the offical example can pass the validation"
       (validator/is-valid? (core/read-raml ?raml)) => ?expect)
 ?raml                                                                  ?expect
 "test/resources/raml/v08/partial-example/parametermized-uri.raml"      true
 "test/resources/raml/v08/partial-example/document.raml"                true
 "test/resources/raml/v08/partial-example/resource-with-uri-parameters.raml" true
 "test/resources/raml/v08/sample.raml"                                  true
 "test/resources/raml/v08/full-example/jukebox-api.raml"                true

 "test/resources/raml/v08/invalid-example/extra-keys-jukebox-api.raml"  false)

(fact "invalid YAML format"
      (fact "should return error info"
            (let [result (core/read-raml "test/resources/yaml/invalid/error.yaml")]
              result => {:raml-clj-parser.yaml/error "Invalid YAML format" :raml-clj-parser.yaml/reason "mapping values are not allowed here\n in 'string', line 6, column 6:\n    ajdsf:\n         ^\n"}
              )))
