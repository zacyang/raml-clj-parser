(ns raml-clj-parser.validator
  (:require [clojure.set :as set]
            [clojure.data :as data]
            [clojure.java.io :as io]
            [schema.core :as s]
            ))

(defn is-url?  [i]
  (try
    (when (io/as-url i) true)
    (catch java.net.MalformedURLException e false)))

(def Root
  {(s/required-key :title)         s/Str
   (s/required-key :baseUri)       (s/pred is-url?)
   (s/optional-key :version)       s/Str
   (s/optional-key :protocols)     s/Str
   (s/optional-key :schemas)       s/Str
   (s/optional-key :documentation) s/Str})

(defn- versioning-base-uri? [baseUri]
  (not (nil? (re-find  #"\{version\}" baseUri))))

(defn- validate-url-parameter [raml]
  (if (versioning-base-uri? (:baseUri raml))
    (when-not (contains? raml :version) {:error {:version "you specified version in baseUri, version tag is needed"}})))

(defn- is-valid-root-elements?
  "https://github.com/raml-org/raml-spec/blob/master/versions/raml-08/raml-08.md#root-section"
  [raml]
  (if-let [pre_fly (validate-url-parameter raml)]
    pre_fly
    (try
      (s/validate Root raml)
      (catch Exception e (.getData e)))
    ))

(defn is-valid? [raml])
