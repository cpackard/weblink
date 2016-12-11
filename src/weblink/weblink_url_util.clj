(in-ns 'weblink.weblink)

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
