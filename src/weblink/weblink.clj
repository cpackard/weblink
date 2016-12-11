(ns weblink.weblink
  (:gen-class)
  (:require [clojure.set :as set]
            [pl.danieljanus.tagsoup :as tagsoup]
            [clojure.java.io :as io]
            [clojure.data.json :as json])
  (:use [flatland.ordered.map]
        [clojure.pprint]))

(load "weblink_json")
(load "weblink_url_util")
(load "weblink_search_util")
(load "weblink_search")

(defn -main
  [& args]
  (if (not= (count args) 2)
    (println "Please supply an initial url and a goal url.")
    (let [[results downward upward] (bidirectional-search (first args) (second args))]
      (pprint results)
      (with-open [out-file (io/writer "www/bidir.json" :append false)]
        (json/write {:downward {:direction "downward"
                                :name "origin"
                                :children [(filter-json-data downward results)]}
                     :upward   {:direction "upward"
                                :name "origin"
                                :children [(filter-json-data upward results)]}}
                    out-file)))
    ))
