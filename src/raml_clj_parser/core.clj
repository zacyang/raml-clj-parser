(ns raml-clj-parser.core
  (:require [clojure.java.io :as io]
            [raml-clj-parser.reader :as reader]))

(defn- when-exist [path]
  (when  (.exists (io/as-file path))
    (slurp path)))

(defn read-raml [path]
  (if-let  [content (when-exist path)]
    (reader/read content)
    {:error "file not exist"}))
