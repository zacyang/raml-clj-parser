(ns raml-clj-parser.tags
  (:require [clojure.string :as str]))

(def ^:const TAG_NAME "!include")

(defn is-raml-resource? [path]
  (str/ends-with? path ".raml"))

(defrecord RamlIncludeTag [tag base_path path content])

(defn- get-resource [path]
  (if-not (is-raml-resource? path)
    (try
      (slurp path)
      (catch Exception e {:error  "resource is not available"}))))
                                        ;(get-resource (str base_path "/" path))
(defn include-tag-ctor-fn
  [base_path tag path]
  (->RamlIncludeTag tag base_path path nil))

(defrecord UnkownTag [tag value])

(defn unkown-tag-ctor-fn
  [tag value]
  (->UnkownTag tag value))
