# raml-clj-parser [![Build Status](https://travis-ci.org/zacyang/raml-clj-parser.svg?branch=master)](https://travis-ci.org/zacyang/raml-clj-parser) [![codecov](https://codecov.io/gh/zacyang/raml-clj-parser/branch/master/graph/badge.svg)](https://codecov.io/gh/zacyang/raml-clj-parser)
This is a RAML parser implemented in clojure, which is based on SnakeYaml libarary.

It is able to parse a given raml file into clojure map, including all the included resource (tag !include) and subordinate raml files.

It also provides validtion according to [raml spec v08](https://github.com/raml-org/raml-spec/blob/master/versions/raml-08/raml-08.md)

## Version

[![Clojars Project](https://img.shields.io/clojars/v/raml-clj-parser.svg)](https://clojars.org/raml-clj-parser)

## Installation

Add the following dependency to your `project.clj` file:

    [raml-clj-parser "0.1.1-SNAPSHOT"]

## Usage

Read raml file into clojure map

```clojure
(require '[raml-clj-parser.core :as raml])

(raml/read-raml "resource/sample.raml")

;; => {:raml-version "0.8", :title "Jukebox API", :baseUri {:uri "http://jukebox.api.com", :raml-clj-parser.reader/uri-parameters []}, :version "v1"}

```
It will convert all the raml tag into clojure keywords and all the routes into string.
And you can use regular clojure fn to navigate though the map and retrieve associated values.


Validate raml content [optional]

The validation is build on Schema for more informatino you could refer to https://github.com/plumatic/schema
```clojure
(require '[raml-clj-parser.core :as raml])

;;valid raml file
(let [raml_content (raml/read-raml "resource/valid.raml")]
  (raml/validate raml_content))

;; valid raml will return its content
;; => {:raml-version "0.8", :title "Jukebox API", :baseUri {:uri "http://jukebox.api.com", :raml-clj-parser.reader/uri-parameters []}, :version "v1"}

;;invalid raml file
(let [raml_content (raml/read-raml "resource/invalid.raml")
      validation_result (raml/validate raml_content)]
    (:error validation_result)

;; invalid raml will return a map contains the :error for the violation
;; in this case, there some key missing in the raml file
;; => {:title missing-required-key, :raml-version missing-required-key, :baseUri missing-required-key, (not ("Resource must starts with /" :error)) invalid-key}

```

Util fn (WIP)
```clojure
;;for a parsed raml file
(get-abs-uri raml ["/sub_uri" "/sub_uri"])
;;=> will return a map contains the leaf resources's information including uri parameter and header

```

## License

Copyright © 2017 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
