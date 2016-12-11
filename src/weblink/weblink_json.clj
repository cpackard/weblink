(in-ns 'weblink.weblink)

(defn get-relevant-name
  "For a given URL, return only the last part of the URL
  (primarily for Wikipedia pages)"
  [url]
  (if (.contains url "/")
    (subs url (inc (.lastIndexOf url "/")))
    url))

(defn convert-to-json-format
  "Take the current url -> child page mapping and recursively
  convert it into the needed format for JSON conversion."
  [[parent children]]
  (if (empty? children)
    {:name (get-relevant-name parent) :children []}
    {:name (get-relevant-name parent)
     :children (into []
                     (map #(convert-to-json-format %)
                          children))
     }))

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
                       (into [] (map #(filter-json-data % results)
                                     (:children json-data))))
     }))
