(defproject raml-clj-parser "0.1.0-SNAPSHOT"
  :description "A clojure implementation of RAML parser"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :author "Yang Yang"

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.yaml/snakeyaml "1.17"]]

  :profiles {:dev {:dependencies   [[midje "1.8.2"]]
                   :resource-paths ["src/test/resources"]
                   :plugins        [[lein-midje "3.2.1"]]}})
