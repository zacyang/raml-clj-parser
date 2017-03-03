(ns raml-clj-parser.reader
  (:refer-clojure :exclude [read])
  (:require [raml-clj-parser.yaml :as yaml ]
            [clojure.string :as str]
            [raml-clj-parser.util :as util]
            [raml-clj-parser.tags :as tags])
  (:import [raml_clj_parser.tags RamlIncludeTag
            RamlError]
           [java.io
            BufferedReader
            StringReader]))

(declare read)

(defprotocol RamlReader
  (->clj [node]))

(def ^:const REGEX_FIRST_LINE "^#%RAML\\s(1\\.0|0\\.8)(\\s+)?$")
(def ^:const ERR_INVALID_FIRST_LINE (tags/->RamlError  "Invalid first line, first line should be #%RAML 0.8 or #%RAML 1.0"))
(def ^:const ERR_FILE_NOT_EXISTS  (tags/->RamlError   "Resource is not available"))

(defn- is-url-path? [i]
  (str/starts-with? i "/"))

(defn- get-valid-key[k]
  (if  (is-url-path? k)
    (str "/" k)
    k))

(defn- to-clj-key [key]
  (cond (keyword? key) key
        (string? key)  (keyword (get-valid-key key))
        :default       (prn-str key)))

(defn is-raml-resource? [path]
  (str/ends-with? path ".raml"))

(defn- has-error? [node]
  (not (nil? (get-in node [:content  :error]))))

(defn- get-resource-content [file_content_path original_path]
  (let [content (first file_content_path)
        path    (second file_content_path)]
    (if (is-raml-resource? original_path)
      (read content path)
      content)))

(defn- get-external-resource [node]
  (if-let [file_content_path (util/when-exist (str (:base_path node) "/" (:path node)))]
    (get-resource-content file_content_path (:path node))
    ERR_FILE_NOT_EXISTS))

(defn- extract-uri-parameters [uri]
  (vec (re-seq #"(?<=\{).*?(?=\})" uri)))

(defn- is-rest-resource-path?[p]
  (str/starts-with? p "/"))

(defn- to-map [k v]
  (let [raml_key   (to-clj-key k)
        raml_value (->clj v)]
    (cond (= k "baseUri")
          [raml_key { :uri            raml_value
                     ::uri-parameters (extract-uri-parameters v)}]
          (is-rest-resource-path? k)
          [k (merge  { :uri            k
                      ::uri-parameters (extract-uri-parameters k)
                      } raml_value)]
          :default
          [raml_key raml_value])))

(extend-protocol RamlReader

  java.util.LinkedHashMap
  (->clj [node]
    (into {}
          (for [[k v] node]
            (to-map k v))))

  java.util.LinkedHashSet
  (->clj [node]
    (set node))

  java.util.ArrayList
  (->clj[node] (vec (map ->clj node)))

  Object
  (->clj [node] node)

  nil
  (->clj [node] nil)

  RamlIncludeTag
  (->clj [node]
    (if (has-error? node)
      node
      (get-external-resource node))))

(defn- is-valid-first-line? [line]
  (.matches line REGEX_FIRST_LINE))

(defn- get-raml-version [content]
  (let [all_lines  (-> content
                       (StringReader.)
                       (BufferedReader.)
                       line-seq)
        first_line (first all_lines)]
    (if (is-valid-first-line? first_line)
      (second (str/split first_line #" "))
      ERR_INVALID_FIRST_LINE)))

(defn- generate-edn [version edn_yaml]
  (merge {:raml-version version} edn_yaml))

(defn- load-raml-content [content base_path]
  (let [raw_yaml (yaml/load content base_path)]
    (if (:raml-clj-parser.yaml/error raw_yaml)
      raw_yaml
      (->clj raw_yaml))))

(defn read [content base_path]
  (let [raml_version (get-raml-version content)
        yaml_content (load-raml-content content base_path)]
    (cond (= ERR_INVALID_FIRST_LINE raml_version) ERR_INVALID_FIRST_LINE
          (:raml-clj-parser.yaml/error yaml_content)             yaml_content
          :default                                (generate-edn raml_version yaml_content))))
