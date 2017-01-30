(ns raml-clj-parser.tags
  (:require [clojure.string :as str]))

(def ^:const INCLUDE_TAG "!include")

(defrecord RamlIncludeTag [tag base_path path content])

(defn include-tag-ctor-fn
  [base_path tag path]
  (->RamlIncludeTag tag base_path path nil))

(defrecord UnkownTag [tag value])

(defn unkown-tag-ctor-fn
  [tag value]
  (->UnkownTag tag value))

(defrecord RamlError [error])
