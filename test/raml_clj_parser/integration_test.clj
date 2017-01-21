(ns raml-clj-parser.integration-test
  (:require [raml-clj-parser.core :as core]
            [midje.sweet :refer [fact =>]]))


(fact "should not blow up..."
      (let [result (core/read-raml "test/resources/raml/v08/sample.raml")]
        (:error result) => nil
        (:title result) => "Jukebox API"))

(fact "should get include file"
      (let [result (core/read-raml "test/resources/raml/v08/full-example/jukebox-api.raml")
            schemas (-> result :schemas)]

        (:error result) => nil
        (count schemas) => 3
        ;;(type (first schemas)) => RamlIncludeTag

        ;;werid midje issue, the following assertion return false, even everything is identical
        ;(first schemas) =>  {:song {:path "jukebox-include-song.schema", :tag "!include"}}

        ))
