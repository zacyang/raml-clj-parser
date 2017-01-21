(ns raml-clj-parser.core
  (:require [clojure.java.io :as io]
            [raml-clj-parser.reader :as reader]))

(defn- when-exist [path]
  (let [file  (io/as-file path) ]
    (when  (and (.exists file)
                (not (.isDirectory file)))
      [(slurp path) (.getParent file)])))

(defn read-raml [path]
  (if-let  [[content base_url] (when-exist path)]
    (reader/read content base_url)
    {:error "file not exist"}))
