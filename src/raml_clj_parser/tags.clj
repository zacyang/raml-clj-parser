(ns raml-clj-parser.tags)

(defprotocol RamlDataCreator
  (->clj [node]))

(def ^:const TAG_NAME "!include")

(defrecord RamlIncludeTag [tag path content])

(defn- get-resource [path]
  (try
    (slurp path)
    (catch Exception e {:error  "resource is not available"})))

(defn include-tag-ctor-fn
  [base_path tag path]
  (->RamlIncludeTag tag path (get-resource (str base_path "/" path))))
