(ns raml-clj-parser.yaml
  (:refer-clojure :exclude [load])
  (:require [raml-clj-parser.tags :as tags])
  (:import [org.yaml.snakeyaml Yaml]
           [org.yaml.snakeyaml.constructor
            AbstractConstruct
            SafeConstructor
            BaseConstructor]
           [org.yaml.snakeyaml.nodes
            ScalarNode
            Tag
            ]
           [org.yaml.snakeyaml.error YAMLException]))

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

(defn- create-tag-constructor [tag ctor_fn yaml_ctor]
  (proxy [AbstractConstruct] []
    (construct [node]
      (let [scalar-node (cast ScalarNode node)
            scalar (call-method BaseConstructor 'constructScalar [ScalarNode] yaml_ctor scalar-node)
            str-val (cast String scalar)]
        ;;TODO new tag name?
        (ctor_fn tag str-val)))))

(defn- register-tag-ctors [tag tag_ctor yaml_ctor]
  (let [get-valid-tag (fn [v](when-not (nil? v)  (Tag. v)))]
    (.put (get-field BaseConstructor 'yamlConstructors yaml_ctor)
          (get-valid-tag tag)
          tag_ctor)))

(defn tag-constructor
  "Creates a custom SafeConstructor that understands the given tag"
  [path]
  (let [yaml_ctor (SafeConstructor.)]
    (register-tag-ctors tags/INCLUDE_TAG (create-tag-constructor tags/INCLUDE_TAG (partial  tags/include-tag-ctor-fn path) yaml_ctor) yaml_ctor)
    (register-tag-ctors nil (create-tag-constructor nil  tags/unkown-tag-ctor-fn yaml_ctor) yaml_ctor)
    yaml_ctor))

(defn- create-yaml [path]
  (Yaml. (tag-constructor path)))

(defn load
  [content path]
  (try
    (.load (create-yaml path) content)
    (catch YAMLException e {::error "Invalid YAML format"
                            ::reason (.getMessage e)})))
