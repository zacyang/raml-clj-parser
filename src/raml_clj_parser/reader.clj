(ns raml-clj-parser.reader
  (:refer-clojure :exclude [read])
  (:require [raml-clj-parser.yaml :as yaml ]
            [clojure.string :as str]
            :reload-all)
  )

(defrecord RamlIncludeTag [tag path])

(defrecord RamlSubUrlTag [tag content])

(defn include-tag-ctor-fn
  [tag str-val]
  (->RamlIncludeTag tag str-val))

(defprotocol SnakeYamlReader
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

(extend-protocol SnakeYamlReader

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
  (->clj [node] nil)

  RamlIncludeTag
  (->clj [node] (into {} node)))

(defn read [content]
  (let [raw_yaml (yaml/load content "!include" include-tag-ctor-fn)]
    (->clj raw_yaml)))
