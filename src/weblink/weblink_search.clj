(in-ns 'weblink.weblink)

(defrecord StartUrl [url frontier explored children])

(defn init-start-url
  [url]
  (->StartUrl url (ordered-map url [nil :true]) {} {url {}}))

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

(defn update-search-findings
  "Update the search based on the new child links from the current page"
  [i url parent has-parent children new-frontier]
  (reduce (fn [m [k v]] (assoc m k v))
          i
          [[:explored (assoc (:explored i) url [parent has-parent])]
           [:frontier new-frontier]
           [:children (add-to-child-data url
                                         children
                                         (get-parents url (:frontier i) (:explored i))
                                         (:children i))]]))

(defn return-solution
  "Given two url's with a valid path between them,
  find and return the solution."
  [url parent has-parent i g]
  (let [all-links-g (merge (:frontier g) (:explored g))
        all-links-i (merge (:frontier i) (:explored i))]
    (if (= (clean-url url) (clean-url (:url g)))
      ; Found path from initial to goal, return path from initial to goal
      (reverse (take-while #(not (nil? %))
                           (find-parents url all-links-i)))
      ; Paths met in the middle, find path in both sets and return final result
      (let [parents-i (take-while #(not (nil? %))
                                  (find-parents parent all-links-i))
            parents-g (take-while #(not (nil? %))
                                  (find-parents url all-links-g))]
        (if (has-all-parents? [url [parent has-parent]] all-links-i)
          (concat (reverse parents-g) parents-i)
          (concat (reverse parents-i) parents-g))))))

(defn bidirectional-search
  [initial goal]
  (loop [i (init-start-url initial)
         g (init-start-url goal)]
    (if (and (empty? (:frontier i)) (empty? (:frontier g)))
      nil
      (if (not (empty? (:frontier i)))
        (let [[url [parent has-parent]] (first (:frontier i))]
          (if (has-valid-path? url parent has-parent i g)
            [(return-solution url parent has-parent i g)
             (convert-to-json-format (first (:children i)))
             (convert-to-json-format (first (:children g)))]
            ; TODO Since grabbing all these links takes such a long time, we
            ; could check if the link is in the explored queue BEFORE searching.
            (let [links (get-links url)
                  children (filter #(not (get (:explored i) %)) links)
                  new-frontier (update-frontier (:frontier i) children url)
                  has-parent (some #(= (clean-url url) (clean-url %)) links)]
              (recur g
                     (update-search-findings i url parent has-parent
                                             children new-frontier)))))
        (recur g i)))))
