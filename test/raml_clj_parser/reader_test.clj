(ns raml-clj-parser.reader-test
  (:import [raml_clj_parser.tags RamlIncludeTag])
  (:require [raml-clj-parser.reader :as sut]
            [raml-clj-parser.yaml :as yaml]
            [raml-clj-parser.tags :as tags]
            [midje.sweet :as midje :refer [facts fact => anything]]))

(fact "should return converted key when parse sub url section"
      (let [java_map_with_slash_in_key
            (doto (new java.util.LinkedHashMap)
              (.put "baseUri" "http://{some-parameters}/domain")) ]
        (sut/->clj java_map_with_slash_in_key) => {:baseUri {:uri "http://{some-parameters}/domain"
                                                                    :raml-clj-parser.reader/uri-parameters ["some-parameters"]}}))

(fact "should return parse url with paramters, and merge the rest of its original content"
      (let [java_map_content_in_resource
            (doto (new java.util.LinkedHashMap)
              (.put "displayname" "User")
              (.put "description" "blas"))
            java_map_with_slash_in_key
            (doto (new java.util.LinkedHashMap)
              (.put "/{parameter}" java_map_content_in_resource)) ]
        (sut/->clj java_map_with_slash_in_key)
        => {"/{parameter}" {:uri                                   "/{parameter}"
                             :raml-clj-parser.reader/uri-parameters ["parameter"]
                             :displayname                           "User"
                             :description                           "blas"}}))

(fact "should convert original key string to clj keyword"
      (let [java_map_with_slash_in_key
            (doto (new java.util.LinkedHashMap)
              (.put "original_key" "value")) ]
        (sut/->clj java_map_with_slash_in_key) => {:original_key "value"}))

(fact "should read raml version infomration"
      (:raml-version (sut/read ..content.. ..base_path..)) => ..RAML_VERSION..
      (provided
       (#'sut/get-raml-version ..content..) => ..RAML_VERSION..
       (yaml/load ..content.. ..base_path..) => ..raw_yaml..))

(fact "should get version from raw yaml"
      (#'sut/get-raml-version  "#%RAML 0.8 \n--- \n otherstuff") => "0.8")

(fact "should return error inform when first line is not raml"
      (sut/read ..raml_with_invalid_first_line.. ..base_path..)
      => {:error "Invalid first line, first line should be #%RAML 0.8 or #%RAML 1.0"}

      (provided
       (#'sut/get-raml-version ..raml_with_invalid_first_line..)
       => sut/ERR_INVALID_FIRST_LINE
       (yaml/load ..raml_with_invalid_first_line.. ..base_path..) => ..raw_yaml..))

(fact "should return false when first is invalid"
      ;;The first line of a RAML API definition document MUST
      ;;begin with the text #%RAML followed by a single space followed
      ;;by the text 1.0 and nothing else before the end of the line.
      (#'sut/is-valid-first-line? "NOT_VALID_FIRST_LINE") => false)

(fact "should return true when first is valid"
      (#'sut/is-valid-first-line?  "#%RAML 0.8") => true)

(fact "should return true when first is valid with space"
      (#'sut/is-valid-first-line?  "#%RAML 0.8 ") => true)

(fact "should return parsed raml when include resource is raml"
      (let [raml_include (tags/->RamlIncludeTag tags/INCLUDE_TAG "test/resources/raml/v08" "small.raml"
                                                nil)]

        (sut/->clj raml_include) => {:baseUri {:uri "http://jukebox.api.com", :raml-clj-parser.reader/uri-parameters []} , :raml-version "0.8", :title "Jukebox API", :version "v1"}))

(fact "should return plain text when include resource is not raml"
      (let [raml_include (tags/include-tag-ctor-fn
                          "test/resources/raml/v08/full-example/"
                          tags/INCLUDE_TAG
                          "jukebox-include-artist.schema")]

        (sut/->clj raml_include) =>  "{\n  \"type\": \"object\",\n  \"$schema\": \"http://json-schema.org/draft-03/schema\",\n  \"required\":false,\n  \"properties\": {\n    \"artistName\": {\n      \"type\": \"string\",\n      \"required\":true\n    },\n    \"description\": {\n      \"type\": \"string\",\n      \"required\": false\n    },\n    \"imageURL\": {\n      \"type\": \"string\",\n      \"required\": false\n    }\n  }\n}\n"))

(fact "should return tag when original content is unavaiable"
      (let [raml_include (tags/->RamlIncludeTag tags/INCLUDE_TAG "test/resources/raml/v08" "anything" {:error "resource is not available"} )]

        (sut/->clj raml_include) =>  {:base_path "test/resources/raml/v08", :content {:error "resource is not available"}, :path "anything", :tag "!include"}))

(fact "should return false for non-raml resources"
      (#'sut/is-raml-resource? "not.txt") => false)

(fact "should return true for raml resources"
      (#'sut/is-raml-resource? "some.raml") => true)

(facts "should create uri parameter type with parameter info, and register to the root"
       (fact "should extract uri parameters to vec"
             (#'sut/extract-uri-parameters "http://localhost/{id}/domain/{name}") => ["id" "name"]
             (#'sut/extract-uri-parameters "http://localhost/domain/") => []))
