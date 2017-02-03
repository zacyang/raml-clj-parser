(defproject raml-clj-parser "0.1.0"
  :description "A clojure implementation of RAML parser"
  :url "https://github.com/zacyang/raml-clj-parser"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :scm {:name "git"
        :url "https://github.com/zacyang/raml-clj-parser"}
  :author "Yang Yang"

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.yaml/snakeyaml "1.17"]
                 [prismatic/schema "1.1.3"]]

  :profiles {:dev {
                   :aliases        {"build" ["do" ["midje"] ["kibit"] ]}
                   :dependencies   [[midje "1.8.3"]]
                   :resource-paths ["src/test/resources"]
                   :plugins        [[lein-midje "3.2.1"]
                                    [lein-kibit "0.1.3"]
                                    [lein-cloverage "1.0.9"]]}})
