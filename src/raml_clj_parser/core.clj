(ns raml-clj-parser.core
  (:require [raml-clj-parser.reader :as reader]
            [raml-clj-parser.util :as util]))

(defn read-raml [path]
  (if-let  [[content base_url] (util/when-exist path)]
    (reader/read content base_url)
    {:error "file not exist"}))
