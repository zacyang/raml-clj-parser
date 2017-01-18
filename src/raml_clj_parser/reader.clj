(ns raml-clj-parser.reader
  (:refer-clojure :exclude [read])
  (:require [raml-clj-parser.yaml :as yaml ] :reload-all))

(defrecord RamlIncludeTag [tag path])

(defn include-tag-ctor-fn
  [tag str-val]
  (->RamlIncludeTag tag str-val))

(defprotocol SnakeYamlReader
  (->clj [node]))

(defn- to-clj-key [key]
  (cond (keyword? key) key
        (string? key) (keyword key)
        :default (prn-str key)))

(extend-protocol SnakeYamlReader

  clojure.lang.IPersistentMap
  (->clj [node]
    (into {}
          (for [[k v] node]
            [(to-clj-key k) (->clj v)])))

  java.util.LinkedHashMap
  (->clj [node]
    (into {}
          (for [[k v] node]
            [(to-clj-key k) (->clj v)])))

  java.util.LinkedHashSet
  (->clj [node]
    (into #{} node))

  java.util.ArrayList
  (->clj[node] (into [] (map ->clj node)))

  Object
  (->clj [node] node)

  nil
  (->clj [node] nil)

  RamlIncludeTag
  (->clj [node] {:tag (:tag node) :path (:path node)}))

(defn read [content]
  (let [raw_yaml (yaml/load content "!include" include-tag-ctor-fn)]
    (->clj raw_yaml)))
