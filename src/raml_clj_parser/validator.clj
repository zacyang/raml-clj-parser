(ns raml-clj-parser.validator
  (:import   [java.io BufferedReader StringReader])
  (:require [clojure.set :as set]
            [clojure.data :as data]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [schema.core :as s]))

(def ^:const VALID_YAML_TYPES ["text/yaml"
                               "text/x-yaml"
                               "application/yaml"
                               "application/x-yaml*"])
(def ^:const CUSTOM_YAML_TYPE "application/[A-Za-z.-0-1]*\\+?(json|xml)")
(defn- is-url?  [i]
  (try
    (when (io/as-url i) true)
    (catch java.net.MalformedURLException e false)))

(def uri (s/pred is-url?  "invalid uri format"))

(defn- versioning-base-uri? [raml]
  (when-let [base_uri (:baseUri raml)]
    (not (nil? (re-find  #"\{version\}" base_uri)))))

(defn- valid-protocols?[c]
  (if (and (coll? c) (seq c))
    (every?  (fn[v] (some #(= % v) ["HTTP" "HTTPS"]))  c)
    false))

(defn- is-one-of-valid-yaml-types? [i]
  (some #(= % i) VALID_YAML_TYPES))

(defn- get-all-IANA-MINE-types[]
  (let [content  (slurp "resources/media_types.txt")
        all_lines  (-> content
                       (StringReader.)
                       (BufferedReader.)
                       line-seq)]
    (map #(second (str/split % #",")) all_lines)))

(def ALL_IANA_MINE (memoize get-all-IANA-MINE-types))

(defn- is-one-of-IANA-MIME? [i]
  (true? (some #(= % i) (ALL_IANA_MINE))))

(defn- is-valid-custom-type? [i]
  (.matches i CUSTOM_YAML_TYPE))

(defn- valid-media-types? [i]
  (and
   (string? i)
   (or
    (is-valid-custom-type? i)
    (is-one-of-valid-yaml-types? i)
    (is-one-of-IANA-MIME? i))))

(defn- valid-schemas? [i]
  (instance? clojure.lang.APersistentMap i))

(def protocols (s/pred valid-protocols?  "protocol only support http and/or https"))
(def media-types (s/pred valid-media-types? "Invalid media type please refer to https://github.com/raml-org/raml-spec/blob/master/versions/raml-08/raml-08.md#default-media-type"))
;;For the sake of readability keep it duplicate
(def optional_version_tag
  {(s/required-key :title)         s/Str
   (s/required-key :baseUri)       uri

   (s/optional-key :version)       s/Str
   (s/optional-key :protocols)     protocols
   (s/optional-key :schemas)       s/Str
   (s/optional-key :documentation) s/Str})

(def mandatory_version_tag
  {(s/required-key :title)         s/Str
   (s/required-key :baseUri)       uri
   (s/required-key :version)       s/Str

   (s/optional-key :protocols)     protocols
   (s/optional-key :schemas)       s/Str
   (s/optional-key :documentation) s/Str})

(def root
  (s/conditional versioning-base-uri?
                 mandatory_version_tag
                 :else
                 optional_version_tag))

(defn- validate-url-parameter [raml]
  (if (versioning-base-uri? raml)
    (when-not (contains? raml :version)
      {:error {:version "you specified version in baseUri, version tag is needed"}})))

(defn- is-valid-root-elements?
  "https://github.com/raml-org/raml-spec/blob/master/versions/raml-08/raml-08.md#root-section"
  [raml]
  (try
    (s/validate root raml)
    (catch Exception e (.getData e))))

(defn is-valid? [raml])
