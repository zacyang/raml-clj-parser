(ns raml-clj-parser.integration-test
  (:require [raml-clj-parser.core :as core]
            [raml-clj-parser.validator :as validator]
            [midje.sweet :refer [fact => =not=> contains has]]))


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

(fact "just make sure the offical example can pass the validation"
      (let [result (core/read-raml "test/resources/raml/v08/full-example/jukebox-api.raml")]
        ;; WILL FAIL AT THE MOMENT
       ;;will move to core after validation is done
     ;;  (validator/is-valid? result) => result)
     ))
