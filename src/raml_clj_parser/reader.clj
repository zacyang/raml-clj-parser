(ns raml-clj-parser.reader
  (:refer-clojure :exclude [read])
  (:require [raml-clj-parser.yaml :as yaml ]
            [clojure.string :as str])
  (:import [raml_clj_parser.tags RamlIncludeTag]
           [java.io
            BufferedReader
            StringReader]))

(defprotocol RamlReader
  (->clj [node]))

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

(extend-protocol RamlReader

  java.util.LinkedHashMap
  (->clj [node]
    (into {}
          (for [[k v] node]
            [(to-clj-key k) (->clj v)])))

  java.util.LinkedHashSet
  (->clj [node]
    (set node))

  java.util.ArrayList
  (->clj[node] (vec (map ->clj node)))

  Object
  (->clj [node] node)

  nil
  (->clj [node] nil))

(def ^:const REGEX_FIRST_LINE "^#%RAML\\s0\\.\\d(\\s+)?$")
(def ^:const ERR_INVALID_FIRST_LINE {:error "Invalid first line, first line should be #%RAML 0.8"})

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
    (->clj raw_yaml)))

(defn read [content base_path]
  (let [raml_version (get-raml-version content)]
    (if-not (= ERR_INVALID_FIRST_LINE raml_version)
      (generate-edn raml_version (load-raml-content content base_path))
      ERR_INVALID_FIRST_LINE)))
