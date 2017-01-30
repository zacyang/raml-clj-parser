(ns raml-clj-parser.core-test
  (:require [raml-clj-parser.core  :as core]
            [clojure.java.io :as io]
            [raml-clj-parser.reader :as reader]
            [raml-clj-parser.util :as util]
            [raml-clj-parser.validator :as validator])
  (:use midje.sweet))

(fact "should parse file when file is exist"
      (core/read-raml ..file_path..) => ..result..
      (provided
       (#'util/when-exist ..file_path..) =>  [..raml_file_content.. ..base_path..]
       (reader/read ..raml_file_content.. ..base_path..) => ..result..))

(fact "should return error infor when file is not exist"
      (core/read-raml ..file_path..) => reader/ERR_FILE_NOT_EXISTS
      (provided
       (#'util/when-exist ..file_path..) => nil))

(facts "should get abs uri from result"
       (fact "should get abs uri , when no uri parameter presents"
             (let [simple_uri_result  {:raml-version "0.8", :title "GitHub API", :version "v3", :baseUri {:uri "https://api.github.com", :raml-clj-parser.reader/uri-parameters []}, "/gists" {:uri "/gists", :raml-clj-parser.reader/uri-parameters [], :displayName "Gists", "/public" {:uri "/public", :raml-clj-parser.reader/uri-parameters [], :displayName "Public Gists"}}}]
               (core/get-abs-uri simple_uri_result ["/gists" "/public"])
               => {:uri "https://api.github.com/gists/public"
                   :raml-clj-parser.reader/uri-parameters []}))

       (fact "should get abs uri , when uri parameter presents"
             (let [parameter_uri_result  {:raml-version "0.8", :title "Users API", :version 1, :baseUri {:uri "https://{apiDomain}.someapi.com", :raml-clj-parser.reader/uri-parameters ["apiDomain"]}, "/users" {:uri "/users", :raml-clj-parser.reader/uri-parameters [], :displayName "retrieve all users", :baseUriParameters {:apiDomain {:enum ["api"]}}, "/{userId}/image" {:uri "/{userId}/image", :raml-clj-parser.reader/uri-parameters ["userId"], :displayName "access users pictures", :baseUriParameters {:apiDomain {:enum ["static"]}}}}}]
               (core/get-abs-uri parameter_uri_result ["/users" "/{userId}/image"])
               => {:uri "https://{apiDomain}.someapi.com/users/{userId}/image"
                   :raml-clj-parser.reader/uri-parameters ["apiDomain" "userId"]})))
