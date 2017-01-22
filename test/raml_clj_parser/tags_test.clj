(ns raml-clj-parser.tags-test
  (:require [raml-clj-parser.tags :as sut]
            [midje.sweet :as midje :refer [fact => provided contains]]))

(fact "should get the content of include tag"
      (let [base_path         "/base_path"
            tag_resource_path "path"]
        (sut/include-tag-ctor-fn base_path ..tag_name.. tag_resource_path)
        => {:base_path "/base_path"
            :tag     ..tag_name..
            :path    tag_resource_path
            :content ..content..}

        (provided
         (#'sut/get-resource "/base_path/path")=> ..content..))

      (fact "should get content "
            ;;FIXME need to setup relative path somewehre
            (#'sut/get-resource "test/resources/raml/v08/full-example/jukebox-include-album-new.sample")
            =not=> (contains :error)))

(fact "should generate error when content is not available"
      (#'sut/get-resource "file-on-mars.definite-not-there.txt") => {:error "resource is not available"})
