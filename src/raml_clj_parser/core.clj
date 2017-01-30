(ns raml-clj-parser.core
  (:require [raml-clj-parser.reader :as reader]
            [raml-clj-parser.util :as util]
            [raml-clj-parser.validator :as validator]))

(defn read-raml [path]
  (if-let  [[content base_url] (util/when-exist path)]
    (reader/read content base_url)
    reader/ERR_FILE_NOT_EXISTS))

(defn validate [raml]
  (validator/validate raml))

;;FIXME: walk should be able do the same thing
(defn- extract [ctor-fn raml path_keys]
  (if (empty? path_keys) ""
      (let [path     (first path_keys)
            resource (get raml path)]
        (ctor-fn resource (extract ctor-fn resource (rest path_keys))))))

(defn- uri-parameters [r nest_r] (into (get-in r [:raml-clj-parser.reader/uri-parameters]) nest_r))

(defn- uri [r nest_r] (str (get-in r [:uri]) nest_r))

(defn get-abs-uri [raml keys_to_leaf]
  "will return a map with abs uri and uriParameters if presents"
  (let [base_uri            (get-in raml [:baseUri :uri])
        base_uri_parameters (get-in raml
                                 [:baseUri :raml-clj-parser.reader/uri-parameters])]
    {:uri                                   (str base_uri (extract uri raml keys_to_leaf))
     :raml-clj-parser.reader/uri-parameters (into base_uri_parameters

                                                  (extract uri-parameters raml keys_to_leaf))}))
