(defproject weblink "0.1.0-SNAPSHOT"
  :description "A project for finding a path of links between any two URL's."
  :url "https://github.com/cpackard/weblink/"
  :license {:name "MIT License"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [clj-tagsoup/clj-tagsoup "0.3.0" :exclusions [org.clojure/clojure]]
                 [org.clojure/math.numeric-tower "0.0.4"]
                 [org.flatland/ordered "1.5.3"]
                 [org.clojure/data.json "0.2.6"]]
  :main ^:skip-aot weblink.weblink
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
