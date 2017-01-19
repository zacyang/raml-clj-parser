(ns raml-clj-parser.reader-test
  (:require [raml-clj-parser.reader :as sut]
            [midje.sweet :as midje :refer [fact =>]]))

(fact "should return converted key when parse sub url section"
      (let [java_map_with_slash_in_key
            (doto (new java.util.LinkedHashMap)
              (.put "/original_key" "value")) ]
        (sut/->clj java_map_with_slash_in_key) => {://original_key "value"}))

(fact "should convert original key string to clj keyword"
      (let [java_map_with_slash_in_key
            (doto (new java.util.LinkedHashMap)
              (.put "original_key" "value")) ]
        (sut/->clj java_map_with_slash_in_key) => {:original_key "value"}))
