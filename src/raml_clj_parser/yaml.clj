(ns raml-clj-parser.yaml
  (:refer-clojure :exclude [load])
  (:require [raml-clj-parser.tags :as tags])
  (:import [org.yaml.snakeyaml Yaml]
           [org.yaml.snakeyaml.constructor
            Constructor
            AbstractConstruct
            Construct
            SafeConstructor
            BaseConstructor]
           [org.yaml.snakeyaml.nodes
            ScalarNode
            Tag]))

(defn call-method
  [klass method-name params obj & args]
  (-> klass (.getDeclaredMethod (name method-name)
                                (into-array Class params))
      (doto (.setAccessible true))
      (.invoke obj (into-array Object args))))

(defn get-field
  [klass field-name obj]
  (-> klass (.getDeclaredField (name field-name))
      (doto (.setAccessible true))
      (.get obj)))

(defn tag-constructor
  "Creates a custom SafeConstructor that understands the given tag"
  [tag ctor_fn]
  (let [result (SafeConstructor.)
        constructor (proxy [AbstractConstruct] []
                      (construct [node]
                        (let [scalar-node (cast ScalarNode node)
                              scalar (call-method BaseConstructor 'constructScalar [ScalarNode] result scalar-node)
                              str-val (cast String scalar)]
                          ;;TODO new tag name?
                          (ctor_fn tag str-val)
                          )))]
    (.put (get-field BaseConstructor 'yamlConstructors result)
          (Tag.  tag)
          constructor)
    result))

(defn- create-yaml []
  (Yaml. (tag-constructor tags/TAG_NAME tags/include-tag-ctor-fn)))

(defn load
  [content]
  (.load (create-yaml) content))
