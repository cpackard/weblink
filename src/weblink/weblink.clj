(ns weblink.weblink
  (:gen-class)
  (:require [clojure.set :as set]
            [pl.danieljanus.tagsoup :as tagsoup]
            [clojure.java.io :as io]
            [clojure.data.json :as json])
  (:use [flatland.ordered.map]
        [clojure.pprint]))

(def ren "https://en.wikipedia.org/wiki/Renaissance")
(def swiss "https://en.wikipedia.org/wiki/Swiss_cheese")

;; Blacklist
;; Sorry wikipedia, but this just throws everything off.
(def wiki-donate-page "https://donate.wikimedia.org/wiki/Special:FundraiserRedirector?utm_source=donate&utm_medium=sidebar&utm_campaign=C13_en.wikipedia.org&uselang=en")

(defn possible-prefixes
  "Returns a list of possible prefixes for a given url"
  [url]
  (re-find #"^https?://([\w-\d]+\.)?" url))

(defn blacklisted?
  "Returns true if the input url is blacklisted, otherwise returns false."
  [url]
  (cond (some #{"donate."} (possible-prefixes url)) true
        :else nil))

(defn get-raw-links-iter
  "Returns a list of all strings belonging to an 'href' tag from the input node"
  [node]
  (loop [current node
         result '()]
    (cond (empty? current) result

          (map? (first current))
          (recur (rest current) (cons (:href (first current)) result))

          (vector? (first current))
          (recur (apply conj (rest current) (first current)) result)

          :else (recur (rest current) result))))

(defn get-links
  "Return all links from a given page"
  [node]
  (try
    (remove nil? (flatten (get-raw-links-iter (tagsoup/parse node))))
    (catch Exception e (println (str "caught exception: " (.getMessage e)))
      '())))

(defn clean-url
  "Sanitizes the url by eliminating all text
  preceding the domain to avoid inconsistencies in search."
  [url]
  (if (instance? java.net.URL url)
    (str url)
    (.replaceAll url "^https?://([\\w\\d]+\\.)?" "")))

(defn find-parents
  "Returns a lazy seq containing the url and all parents recursively in m."
  [url m]
  (let [[parent has-parent] (get m url)]
    (lazy-seq (cons url (find-parents parent m)))))

(defn add-parent-domain
  "Given a url in the same page as the parent, return a new url
  of the child sub-domain wth the parent domain prepended."
  [child parent]
  (cond (re-find #"^/|^#" child)
        (let [s (clojure.string/split parent #"/")]
          (if (re-find #"^http" (first s))
            (str (first s) "//" (nth s 2) child)
            (str (first s) child)))

        (and parent (re-find #"^file:" parent))
        (str (second (re-find #"(.*/)(.*)" parent)) child)

        :else child))

(defn has-parent?
  "Given a child url and a parent url,
  determine whether the child has a link to the parent."
  [child parent]
  (let [child-links (try (get-links child)
                         (catch Exception e
                           (println (str "caught exception: " (.getMessage e)))
                           '()))]
    (some #(= (clean-url (add-parent-domain % parent)) (clean-url parent))
          child-links)))

(defn has-all-parents?
  "Returns true if a seq of nodes have links to their parents,
  otherwise returns false."
  [[url [parent has-parent]] m]
  (loop [[p h-p] [parent has-parent]
         prev url]
    (cond (nil? p) true
          ; the second value is the has-parent property
          (and (not h-p) (not (has-parent? prev p))) nil
          :else (recur (get m p) p))))

; TODO write tests for this
(defn has-valid-path?
  "Returns true if there is a valid path between
  the initial url and the goal url."
  [url parent has-parent g
   frontier-i explored-i frontier-g explored-g]
  (or (= (clean-url url) (clean-url g))
      (and (or (get explored-g url) (get frontier-g url))
           ; Make sure at least one we have complete path
           ; either from initial or goal
           (or (has-all-parents? [url [parent has-parent]]
                                 (merge frontier-i explored-i))
               (has-all-parents? [url (get (merge frontier-g explored-g) url)]
                                 (merge frontier-g explored-g))))))

(defn return-solution
  "Given two url's with a valid path between them,
  find and return the solution."
  [url parent has-parent g
   frontier-i explored-i frontier-g explored-g]
  (if (= (clean-url url) (clean-url g))
    ; Found path from initial to goal, return path from initial to goal
    (reverse (take-while #(not (nil? %))
                         (find-parents url (merge frontier-i explored-i))))

    ; Paths met in the middle, find path in both sets and return final result
    (let [parents-i (take-while #(not (nil? %))
                                (find-parents parent
                                              (merge frontier-i explored-i)))
          parents-g (take-while #(not (nil? %))
                                (find-parents url
                                              (merge frontier-g explored-g)))]
      (if (has-all-parents? [url [parent has-parent]]
                            (merge frontier-i explored-i))
        (concat (reverse parents-g) parents-i)
        (concat (reverse parents-i) parents-g)))))

(defn update-frontier
  "Update the given frontier with a list of children
  which were recently explored."
  [frontier-i children url]
  (reduce (fn [result child]
            ; TODO Add-parent-domain to child before checking result
            (if (and (not (blacklisted? child))
                     (not (get result child)))
              (assoc result (add-parent-domain child url) [url nil])
              result))
          (dissoc frontier-i url)
          children))

(defn get-parents
  "Given a url and it's related frontier/explored maps,
  return the list of parents from url -> initial search."
  [url frontier explored]
  (let [parents (take-while #(not (nil? %))
                            (find-parents url (merge frontier explored)))]
    (reverse parents)))

(defn get-relevant-name
  "For a given URL, return only the last part of the URL
  (primarily for Wikipedia pages)"
  [url]
  (if (.contains url "/")
    (subs url (inc (.lastIndexOf url "/")))
    url))

(defn add-to-child-data
  "Update the current mapping of url -> child pages,
  ignoring any intra-page links."
  [url children url-parents search-data]
  (assoc-in search-data url-parents
            (into {} (map #(vector % {})
                          (filter #(not (or (.contains % "#")
                                            (.contains % ".php")
                                            (.contains % ":")))
                                  children)))))

(defn convert-to-json-format
  "Take the current url -> child page mapping and recursively
  convert it into the needed format for JSON conversion."
  [[parent children]]
  (if (empty? children)
    {:name (get-relevant-name parent) :children []}
    {:name (get-relevant-name parent)
     :children (into []
                     (map #(convert-to-json %)
                          children))
     }))

(defn bidirectional-search
  [initial goal]
  (loop [i initial
         g goal
         frontier-i (ordered-map initial [nil :true])
         frontier-g (ordered-map goal [nil :true])
         explored-i {}
         explored-g {}
         children-i {initial {}}
         children-g {goal {}}]
    (if (and (empty? frontier-i) (empty? frontier-g))
      nil
      (if (not (empty? frontier-i))
        (let [[url [parent has-parent]] (first frontier-i)]
          (if (has-valid-path? url parent has-parent g
                               frontier-i explored-i frontier-g explored-g)
            [
             (return-solution url parent has-parent g
                              frontier-i explored-i frontier-g explored-g)
             (convert-to-json-format (first children-i))
             (convert-to-json-format (first children-g))]
            ; TODO Since grabbing all these links takes such a long time, we
            ; could check if the link is in the explored queue BEFORE searching.
            ; No solution yet, find next set of child urls and keep searching
            (let [links (get-links url)
                  children (filter #(not (get explored-i %)) links)
                  new-frontier (update-frontier frontier-i children url)
                  has-parent (some #(= (clean-url url) (clean-url %)) links)]

              ; swap positions to explore the other node
              (recur g i frontier-g new-frontier explored-g
                     (assoc explored-i url [parent has-parent])
                     children-g (add-to-child-data url
                                                   children
                                                   (get-parents url frontier-i explored-i)
                                                   children-i)))))
        (recur g i frontier-g frontier-i explored-g explored-i children-g children-i)))))


(defn filter-json-data
  "Take the exisiting json-formatted data and remove and nodes
  that weren't directly searched."
  [json-data results]
  (if (empty? (:children json-data))
    (if (some #(= % (:name json-data))
              (map #(get-relevant-name %) results))
      json-data
      nil)
    {:name (:name json-data)
     :children (filter #(not (nil? %))
                       (into [] (map #(filter-json % results)
                                     (:children json-data))))
     }))

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
