(ns raml-clj-parser.core-test
  (:require [raml-clj-parser.core  :as core]
            [clojure.java.io :as io]
            [raml-clj-parser.reader :as reader])
  (:use midje.sweet))

(fact "should parse file when file is exist"
      (core/read-raml ..file_path..) => ..result..
      (provided
       (#'core/when-exist ..file_path..) =>  [..raml_file_content.. ..base_path..]
       (reader/read ..raml_file_content.. ..base_path..) => ..result..))

(fact "should return error infor when file is not exist"
      (core/read-raml ..file_path..) => {:error "file not exist"}
      (provided
       (#'core/when-exist ..file_path..) => nil))
