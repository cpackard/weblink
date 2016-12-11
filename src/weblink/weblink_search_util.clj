(in-ns 'weblink.weblink)

(defn find-parents
  "Returns a lazy seq containing the url and all parents recursively in m."
  [url m]
  (let [[parent has-parent] (get m url)]
    (lazy-seq (cons url (find-parents parent m)))))

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
          (and (not h-p) (not (has-parent? prev p))) nil
          :else (recur (get m p) p))))

(defn get-parents
  "Given a url and it's related frontier/explored maps,
  return the list of parents from url -> initial search."
  [url frontier explored]
  (let [parents (take-while #(not (nil? %))
                            (find-parents url (merge frontier explored)))]
    (reverse parents)))

; TODO write tests for this
(defn has-valid-path?
  "Returns true if there is a valid path between
  the initial url and the goal url."
  [url parent has-parent i g]
  (let [all-links-g (merge (:frontier g) (:explored g))
        all-links-i (merge (:frontier i) (:explored i))]
    (or (= (clean-url url) (clean-url (:url g)))
        (and (or (get (:explored g) url) (get (:frontier g) url))
                                        ; Make sure at least one we have complete path
                                        ; either from initial or goal
             (or (has-all-parents? [url [parent has-parent]] all-links-i)
                 (has-all-parents? [url (get all-links-g url)] all-links-g))))))
