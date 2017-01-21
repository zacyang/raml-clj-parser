(ns raml-clj-parser.tags)

(def ^:const TAG_NAME "!include")

(defrecord RamlIncludeTag [tag path content])

(defn include-tag-ctor-fn
  [tag str-val]
  (->RamlIncludeTag tag str-val nil))
