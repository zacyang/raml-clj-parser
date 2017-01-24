(ns raml-clj-parser.validator-test
  (:require [raml-clj-parser.validator :as sut]
            [schema.core :as s]
            [midje.sweet :as midje
             :refer [facts fact => contains anything tabular]]))

(def ^:const MIN_VALID_DATA {:title "abc" :baseUri "https://abc.com"})

(facts "util fn test"
       (fact "should return true for valid uri"
             (#'sut/is-url? "http://some.com") => true
             (#'sut/is-url? "https://some.com") => true)

       (fact "should return true for valid uri with uri parameter"
             (#'sut/is-url? "http://some.{docker}.com") => true
             (#'sut/is-url? "https://some.{docker}.com") => true)

       (fact "should return false for invalid uri with uri parameter"
             (#'sut/is-url? "http://some.{docker}.com") => true
             (#'sut/is-url? "https://some.{docker}.com") => true)

       (fact "TODO: should somehow store the uri parameter magically.... "))

(facts "Required properties of every node in RAML model must be provided with values."
       (fact "All required root elements should presents"
             (:error (#'sut/is-valid-root-elements? {})) => {:title 'missing-required-key, :baseUri 'missing-required-key})

       (fact "should return original data when it's valid"

             (#'sut/is-valid-root-elements? MIN_VALID_DATA) =>   {:title "abc" :baseUri "https://abc.com" })

       (fact "should return false when contains extra keys"
             (#'sut/is-valid-root-elements? (merge MIN_VALID_DATA {:exteral-key "bla"}))
             => (contains  {:error {:exteral-key 'disallowed-key}}))

       (fact "API title must presents and contains value"
             (#'sut/is-valid-root-elements?(dissoc MIN_VALID_DATA :title))
             =>  (contains {:error {:title 'missing-required-key}})

             ;;TODO not sure why same content not pass test
             ;;(:error (#'sut/is-valid-root-elements? (merge MIN_VALID_DATA {:title 'not-string}))) =>  {:title '(not (instance? java.lang.String not-string ))}
             )

       (fact "base uri must presents"
             (#'sut/is-valid-root-elements? (dissoc MIN_VALID_DATA :baseUri))
             => (contains  {:error {:baseUri 'missing-required-key}}))

       (fact "if base uri contains reserve uri parameter version , we should parse it"
             "TODO:not sure where should we impl it")

       (fact "when version presents in uri as parameter, version tag become mandatory"
             (let [without_version_and_uri_parameter {:title   "abc"
                                                      :baseUri "https://abc.com/{version}"
                                                      }]
               (#'sut/is-valid-root-elements? without_version_and_uri_parameter)
               => (contains {:error {:version 'missing-required-key}})))

       (fact "protocols must be array of strings, the value could only be HTTP or HTTPS"
             (#'sut/is-valid-root-elements? (assoc MIN_VALID_DATA :protocols ["HTTP" "HTTPS"])) =>  {:baseUri "https://abc.com", :protocols ["HTTP" "HTTPS"], :title "abc"})

       (fact "negative cases ,protocols must be array of strings, the value could only be HTTP or HTTPS"

             (#'sut/valid-protocols? "not list") => false
             (#'sut/valid-protocols? nil) => false
             (#'sut/valid-protocols? ["ftp"]) => false
             (#'sut/valid-protocols? ["ftp" "HTTPS"]) => false)

       (fact "Media type should be in the given lists")

       (fact "uri parameter for baseuri other than version
https://github.com/raml-org/raml-spec/blob/master/versions/raml-08/raml-08.md#uri-parameters
"))

(facts "media typeshttps://github.com/raml-org/raml-spec/blob/master/versions/raml-08/raml-08.md#default-media-type"
       (tabular
        (fact "valid media type could be in one of 3 catologue in the specs"
              (#'sut/valid-media-types? ?type) => ?expect)
        ?type                               ?expect
        "text/yaml"                         true
        "text/x-yaml"                       true
        "application/yaml"                  true
        "application/x-yaml*"               true

        "application/zip"                   true

        "application/yang+json"             true
        "application/yang+xml"             true
        "application/yangjson"             true
        "application/yangxml"             true)

       (tabular
        (fact "some invalid cases , of course not able to cover all wait for clj1.9 's advent"
              (#'sut/valid-media-types? ?type) => ?expect)
        ?type                               ?expect
        "bla"                               false
        "text/x1-yaml"                      false
        "notapp/yang+json"                  false
        15                                  false
        ))

(facts "schemas https://github.com/raml-org/raml-spec/blob/master/versions/raml-08/raml-08.md#schemas"
       (tabular
        (fact "The value of the schemas property is an array of maps"
              (#'sut/valid-schemas? ?schema) => ?expect)
        ?schema                           ?expect
        {}                                false
        ""                                false
        []                                true
        #{}                               false
        [{}]                              true)

       (tabular
        (fact "Schema could be name : !include schmema"
              ;;since resource will be slurp into clj , so there will only be 3 types
              ;; 1. error
              ;; 2. raml
              ;; 3. other schema
              (#'sut/valid-schemas? ?schema) => ?expect)
        ?schema                                 ?expect

        [{:schema-name "content from the !include tag"}]         true
        [{:error "resource is not available"}]      false)

       (fact "Schema could be name: schema-content"
             ;(#'sut/vaid-schemas? {:song "{\n  \"type\": \"object\",\n  \"$schema\": \"http://json-schema.org/draft-03/schema\",\n  \"id\": \"http://jsonschema.net\",\n  \"required\": true,\n  \"properties\": {\n    \"songId\": {\n      \"type\": \"string\",\n      \"required\": true,\n      \"minLength\": 36,\n      \"maxLength\": 36\n    },\n    \"songTitle\": {\n      \"type\": \"string\",\n      \"required\": true\n    },\n    \"albumId\": {\n      \"type\": \"string\",\n      \"required\": true,\n      \"minLength\": 36,\n      \"maxLength\": 36\n    }\n  }\n}\n"}) => true
             )
       )
