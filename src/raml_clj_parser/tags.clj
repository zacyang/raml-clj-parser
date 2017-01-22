(ns raml-clj-parser.tags
  (:require [clojure.string :as str]))

(defprotocol RamlDataCreator
  (->clj [node]))

(def ^:const TAG_NAME "!include")

(defn is-raml-resource? [path]
  (str/ends-with? path ".raml"))

(defrecord RamlIncludeTag [tag base_path path content])

(defn- get-resource [path]
  (if-not (is-raml-resource? path)
    (try
      (slurp path)
      (catch Exception e {:error  "resource is not available"}))))

(defn include-tag-ctor-fn
  [base_path tag path]
  (->RamlIncludeTag tag base_path path (get-resource (str base_path "/" path))))
