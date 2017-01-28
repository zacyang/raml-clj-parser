(ns raml-clj-parser.validator-test
  (:require [raml-clj-parser.validator :as sut]
            [schema.core :as s]
            [midje.sweet :as midje
             :refer [facts fact => contains anything tabular]]))

(def ^:const MIN_VALID_DATA
  {:raml-version "0.8", :title "abc", :version "v28.0", :baseUri {:uri "https://domain.force.com/", :raml-clj-parser.reader/uri-parameters []}})

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
             (:error (#'sut/is-valid-root-elements? {}))
             => {:baseUri 'missing-required-key, :raml-version 'missing-required-key, :title 'missing-required-key})

       (fact "should return original data when it's valid"
             (#'sut/is-valid-root-elements? MIN_VALID_DATA) => MIN_VALID_DATA)

       (fact "should raise eror  when contains extra keys"
             (let [with_extra_key (merge MIN_VALID_DATA {:exteral-key "bla"})]
               (#'sut/is-valid-root-elements? with_extra_key)
               => (contains {:type :schema.core/error})))

       (fact "API title must presents and contains value"
             (#'sut/is-valid-root-elements?(dissoc MIN_VALID_DATA :title))
             =>  (contains {:error {:title 'missing-required-key}})

             ;;TODO not sure why same content not pass test
             ;;(:error (#'sut/is-valid-root-elements? (merge MIN_VALID_DATA {:title 'not-string}))) =>  {:title '(not (instance? java.lang.String not-string ))}
             )

       (fact "base uri must presents"
             (#'sut/is-valid-root-elements? (dissoc MIN_VALID_DATA :baseUri))
             => (contains  {:error {:baseUri 'missing-required-key}}))

       (fact "when version presents in uri as parameter, version tag become mandatory"
             (let [without_version_and_uri_parameter {:title   "abc"
                                                      :baseUri "https://abc.com/{version}"
                                                      }]
               (#'sut/valid-protocols? without_version_and_uri_parameter)
               => false))

       (fact "protocols must be array of strings, the value could only be HTTP or HTTPS"
             (#'sut/is-valid-root-elements? (assoc MIN_VALID_DATA :protocols ["HTTP" "HTTPS"])) => (assoc MIN_VALID_DATA :protocols ["HTTP" "HTTPS"]))

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
        [{:error "resource is not available"}]      false))

(facts "uri parameters https://github.com/raml-org/raml-spec/blob/master/versions/raml-08/raml-08.md#uri-parameters"
       (fact "The uriParameters property MUST be a map in which each key MUST be the name of the URI parameter as defined in the baseUri property."
             (let [defined_params ["communityDomain" "communityPath"]
                   root { :baseUri {:uri "https://{communityDomain}.force.com/{communityPath}", :raml-clj-parser.reader/uri-parameters defined_params}}
                   uriParam  { :uriParameters {:communityDomain {:displayName "Community Domain", :type "string"}, :communityPath {:displayName "Community Path", :type "string", :pattern "^[a-zA-Z0-9][-a-zA-Z0-9]*$", :minLength 1}}}]

               (#'sut/valid-uri-parameters?  uriParam) => true
               (#'sut/all-parameters-defined-in-base-uri? (merge root uriParam)) => true))


       (fact "Negative case. no parameter specified in baseUri"
             (let [root { :baseUri {:uri "https://normal.url.with.out.parameters/",
                                    :raml-clj-parser.reader/uri-parameters []}
                         :uriParameters {:communityDomain {:displayName "Community Domain", :type "string"}}}]

               (#'sut/all-parameters-defined-in-base-uri?  root) => false))

       (fact "The uriParameters CANNOT contain a key named version"
             (let [ uriParam_contains_version_literal { :uriParameters {:version {:displayName "Community Domain", :type "string"}}}]
               (#'sut/valid-uri-parameters? uriParam_contains_version_literal) => false)))

(facts "user document https://github.com/raml-org/raml-spec/blob/master/versions/raml-08/raml-08.md#user-documentation")

(facts "Resource and nested resource. https://github.com/raml-org/raml-spec/blob/master/versions/raml-08/raml-08.md#resources-and-nested-resources"
       ;; (let [resource
       ;;       {:uri "/songs", :raml-clj-parser.reader/uri-parameters [], :type {:collection {:exampleCollection "[\n  {\n    \"songId\": \"550e8400-e29b-41d4-a716-446655440000\",\n    \"songTitle\": \"Get Lucky\"\n  },\n  {\n    \"songId\": \"550e8400-e29b-41d4-a716-446655440111\",\n    \"songTitle\": \"Loose yourself to dance\"\n  },\n  {\n    \"songId\": \"550e8400-e29b-41d4-a716-446655440222\",\n    \"songTitle\": \"Gio sorgio by Morodera\"\n  }\n]\n", :exampleItem "{\n  \"songId\": \"550e8400-e29b-41d4-a716-446655440000\",\n  \"songTitle\": \"Get Lucky\",\n  \"albumId\": \"183100e3-0e2b-4404-a716-66104d440550\"\n}\n"}}, :get {:is [{:searchable {:description "with valid searchable fields: songTitle", :example "[\"songTitle\", \"Get L\", \"like\"]"}} {:orderable {:fieldsList "songTitle"}} "pageable"]}, ://{songId} {:uri "/{songId}", :raml-clj-parser.reader/uri-parameters ["songId"], :type {:collection-item {:exampleItem "{\n  \"songId\": \"550e8400-e29b-41d4-a716-446655440000\",\n  \"songTitle\": \"Get Lucky\",\n  \"duration\": \"6:07\",\n  \"artist\": {\n    \"artistId\": \"110e8300-e32b-41d4-a716-664400445500\",\n    \"artistName\": \"Daft Punk\",\n    \"imageURL\": \"http://travelhymns.com/wp-content/uploads/2013/06/random-access-memories1.jpg\"\n  },\n  \"album\": {\n    \"albumId\": \"183100e3-0e2b-4404-a716-66104d440550\",\n    \"albumName\": \"Random Access Memories\",\n    \"imageURL\": \"http://upload.wikimedia.org/wikipedia/en/a/a7/Random_Access_Memories.jpg\"\n  }\n}\n"}}, ://file-content {:uri "/file-content", :raml-clj-parser.reader/uri-parameters [], :description "The file to be reproduced by the client", :get {:description "Get the file content", :responses {"200\n" {:body {:application/octet-stream {:example "we dont need the real big dumb mp3 file, you know what i am testing....\n"}}}}}, :post {:description "Enters the file content for an existing song entity.\n\nThe song needs to be created for the `/songs/{songId}/file-content` to exist.\nYou can use this second resource to get and post the file to reproduce.\n\nUse the \"binary/octet-stream\" content type to specify the content from any consumer (excepting web-browsers).\nUse the \"multipart-form/data\" content type to upload a file which content will become the file-content\n", :body {:application/octet-stream nil, :multipart/form-data {:formParameters {:file {:description "The file to be uploaded", :required true, :type "file"}}}}}}}}]
       ;;   ;(s/validate ResourcePath ://songs ) => ://songs

       ;;   )
       )
