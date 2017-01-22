(ns raml-clj-parser.util
  (:require [clojure.java.io :as io]))

(defn when-exist [path]
  (let [file  (io/as-file path) ]
    (when  (and (.exists file)
                (not (.isDirectory file)))
      [(slurp path) (.getParent file)])))
