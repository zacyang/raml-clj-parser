(ns raml-clj-parser.core
  (:require [raml-clj-parser.reader :as reader]
            [raml-clj-parser.util :as util]

            ))

(defn read-raml [path]
  (if-let  [[content base_url] (util/when-exist path)]
    (reader/read content base_url)
    {:error "file not exist"}))

(defn- get-uri-parameters [raml path_keys ]
  (if (empty? path_keys) ""
      (let [path     (first path_keys)
            resource (get raml path)]

        (into (get-in  resource [:raml-clj-parser.reader/uri-parameters]) (get-uri-parameters resource  (rest path_keys))))))

(defn- get-uri [raml path_keys]
  (if (empty? path_keys) ""
      (let [path     (first path_keys)
            resource (get raml path)]
        (str (get-in  resource [:uri]) (get-uri resource  (rest path_keys))))))

(defn get-abs-uri [raml keys_to_leaf]
  "will return a map with abs uri and uriParameters if presents"
  (let [base_uri            (get-in raml [:baseUri :uri])
        base_uri_parameters (get-in raml
                                 [:baseUri :raml-clj-parser.reader/uri-parameters])]
    {:uri                                   (str base_uri (get-uri raml keys_to_leaf))
     :raml-clj-parser.reader/uri-parameters (into base_uri_parameters (get-uri-parameters raml keys_to_leaf))}
    ))
