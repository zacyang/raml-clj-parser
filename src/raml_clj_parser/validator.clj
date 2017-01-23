(ns raml-clj-parser.validator
  (:require [clojure.set :as set]
            [clojure.data :as data]))

(def ^:const ROOT_ELMENTS {:required [:title
                                      :baseUri ;;Optional during development; Required after implementation

                                      ]
                           :optional [:version
                                      :protocols
                                      :mediaType
                                      :schemas
                                      :documentation
                                      ]})

(defn- no-extra-keys? [raml]
  (let [[required_keys optional_keys] (vals ROOT_ELMENTS)
        all_keys                      (concat required_keys optional_keys)
        only_presents_in_raml         (first (data/diff (keys raml) all_keys))]
    (nil? only_presents_in_raml)))

(defn- all-required-key-presents? [raml])

(defn- all-value-valid? [raml])

(defn- is-valid-root-elements?
  "https://github.com/raml-org/raml-spec/blob/master/versions/raml-08/raml-08.md#root-section"
  [raml]
  (and  (no-extra-keys? raml) (all-required-key-presents? raml) (all-value-valid? raml)))

(defn is-valid? [raml])
