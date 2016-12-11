(ns weblink.core-test
  (:require [clojure.test :refer :all]
            [weblink.weblink :as weblink]
            [pl.danieljanus.tagsoup :as tagsoup]
            [clojure.java.io :as io]))

;; Test Data
(def yegge-web "http://steve-yegge.blogspot.com")
(def jython "http://www.jython.org")
(def jython-book "http://www.jythonbook.org")
(def ind_day "https://en.wikipedia.org/wiki/Independence_Day_(United_States)")
(def us "https://en.wikipedia.org/wiki/United_States")
(def fireworks "https://en.wikipedia.org/wiki/Fireworks")
(def model_rocket "https://en.wikipedia.org/wiki/Model_rocket")
(def quality-of-life "https://en.wikipedia.org/wiki/Quality_of_life")


(deftest test-possible-prefixes
  (testing "Possible prefixes"
    (testing "with http"
      (is (= "http://steve-yegge." (first (weblink/possible-prefixes yegge-web))))
      (is (= "http://www." (first (weblink/possible-prefixes jython))))
      (is (not (= "http://" (first (weblink/possible-prefixes us))))))
    (testing "with https"
      (is (= "https://en." (first (weblink/possible-prefixes ind_day))))
      (is (= "https://en." (first (weblink/possible-prefixes us))))
      (is (= "https://en." (first (weblink/possible-prefixes fireworks))))
      (is (= "https://en." (first (weblink/possible-prefixes model_rocket))))
      (is (not (= "http://" (first (weblink/possible-prefixes ind_day))))))))

(deftest test-blacklisted?
  (testing "blacklisted"
    (is (weblink/blacklisted? weblink/wiki-donate-page))
    (is (not (weblink/blacklisted? ind_day)))))

(deftest test-get-raw-links-iter
  (testing "one-way"
    (testing "a.html links"
      (is (not (empty? (weblink/get-raw-links-iter
                        (tagsoup/parse (io/resource "one-way/a.html"))))))
      (is (some #{"b.html"} (weblink/get-raw-links-iter
                             (tagsoup/parse (io/resource "one-way/a.html"))))))
    (testing "b.html lnks"
      (is (not (empty? (weblink/get-raw-links-iter
                        (tagsoup/parse (io/resource "one-way/b.html"))))))
      (is (some #{"c.html"} (weblink/get-raw-links-iter
                             (tagsoup/parse (io/resource "one-way/b.html"))))))
    (testing "c.html links"
      (is (not (empty? (weblink/get-raw-links-iter
                        (tagsoup/parse (io/resource "one-way/c.html"))))))))
  (testing "two-way"
    (testing "a.html links"
      (is (not (empty? (weblink/get-raw-links-iter
                        (tagsoup/parse (io/resource "two-way/a.html"))))))
      (is (some #{"b.html"} (weblink/get-raw-links-iter
                             (tagsoup/parse (io/resource "two-way/a.html"))))))
    (testing "b.html lnks"
      (is (not (empty? (weblink/get-raw-links-iter
                        (tagsoup/parse (io/resource "two-way/b.html"))))))
      (is (some #{"a.html"} (weblink/get-raw-links-iter
                             (tagsoup/parse (io/resource "two-way/b.html")))))
      (is (some #{"c.html"} (weblink/get-raw-links-iter
                             (tagsoup/parse (io/resource "two-way/b.html"))))))
    (testing "c.html links"
      (is (not (empty? (weblink/get-raw-links-iter
                        (tagsoup/parse (io/resource "two-way/c.html"))))))
      (is (some #{"b.html"} (weblink/get-raw-links-iter
                             (tagsoup/parse (io/resource "two-way/c.html")))))))
  (testing "no-path"
    (testing "a.html links"
      (is (not (empty? (weblink/get-raw-links-iter
                        (tagsoup/parse (io/resource "no-path/a.html"))))))
      (is (some #{"b.html"} (weblink/get-raw-links-iter
                             (tagsoup/parse (io/resource "no-path/a.html"))))))
    (testing "b.html lnks"
      (is (not (empty? (weblink/get-raw-links-iter
                        (tagsoup/parse (io/resource "no-path/b.html")))))))
    (testing "c.html links"
      (is (not (empty? (weblink/get-raw-links-iter
                        (tagsoup/parse (io/resource "no-path/c.html"))))))
      (is (some #{"b.html"} (weblink/get-raw-links-iter
                             (tagsoup/parse (io/resource "no-path/c.html")))))))
  )

(deftest test-get-links
  (testing "one-way"
    (testing "a.html links"
      (is (not (empty? (weblink/get-links (io/resource "one-way/a.html")))))
      (is (some #{"b.html"} (weblink/get-links (io/resource "one-way/a.html")))))
    (testing "b.html links"
      (is (not (empty? (weblink/get-links (io/resource "one-way/b.html")))))
      (is (some #{"c.html"} (weblink/get-links (io/resource "one-way/b.html")))))
    (testing "c.html links"
      (is (empty? (weblink/get-links (io/resource "one-way/c.html"))))))
  (testing "two-way"
    (testing "a.html links"
      (is (not (empty? (weblink/get-links (io/resource "two-way/a.html")))))
      (is (some #{"b.html"} (weblink/get-links (io/resource "two-way/a.html")))))
    (testing "b.html links"
      (is (not (empty? (weblink/get-links (io/resource "two-way/b.html")))))
      (is (some #{"a.html"} (weblink/get-links (io/resource "two-way/b.html"))))
      (is (some #{"c.html"} (weblink/get-links (io/resource "two-way/b.html")))))
    (testing "c.html links"
      (is (not (empty? (weblink/get-links (io/resource "two-way/c.html")))))
      (is (some #{"b.html"} (weblink/get-links (io/resource "two-way/c.html"))))))
  (testing "no-path"
    (testing "a.html links"
      (is (not (empty? (weblink/get-links (io/resource "no-path/a.html")))))
      (is (some #{"b.html"} (weblink/get-links (io/resource "no-path/a.html")))))
    (testing "b.html links"
      (is (empty? (weblink/get-links (io/resource "no-path/b.html")))))
    (testing "c.html links"
      (is (not (empty? (weblink/get-links (io/resource "no-path/c.html")))))
      (is (some #{"b.html"} (weblink/get-links (io/resource "no-path/c.html"))))))
  )


(deftest test-clean-url
  (testing "https"
    (is (= "wikipedia.org/wiki/Independence_Day_(United_States)"
           (weblink/clean-url ind_day))))
  (testing "http"
    (is (= "jython.org" (weblink/clean-url jython))))
  (testing "no-protocal"
    (is (= (str (io/resource "one-way/a.html"))
           (weblink/clean-url (io/resource "one-way/a.html"))))))


(deftest test-find-parents
  (let [m {"a" ["b" :true]
           "b" ["c" :true]
           "c" ["d" nil]}]
    (testing "found all parents"
      (is (= '("a" "b" "c" "d")
             (take-while #(not (nil? %)) (weblink/find-parents "a" m)))))))


(deftest test-add-parent-domain
  (testing "wikipedia pages"
    (is (= "https://en.wikipedia.org/wiki/United_States"
           (weblink/add-parent-domain "/wiki/United_States" ind_day))))
  (testing "local files"
    (is (= (str (io/resource "two-way/a.html"))
           (weblink/add-parent-domain "a.html" (str (io/resource "two-way/a.html"))))))
  (testing "other pages"
    (is (= jython
           (weblink/add-parent-domain jython ind_day)))))

(deftest test-has-parent?
  (testing "has valid parent"
    (is (weblink/has-parent? (str (io/resource "two-way/b.html"))
                             (str (io/resource "two-way/a.html")))))
  (testing "no parent"
    (is (not (weblink/has-parent? (str (io/resource "one-way/b.html"))
                                  (str (io/resource "one-way/a.html")))))))


(deftest test-has-all-parents?
  (testing "has all parents - two node seq"
    (is (weblink/has-all-parents? [(str (io/resource "two-way/b.html"))
                                   [(str (io/resource "two-way/a.html")) :true]]
                                  {(str (io/resource "two-way/a.html")) [nil :true]})))
  (testing "has all parents - three node seq"
    (is (weblink/has-all-parents? [(str (io/resource "two-way/c.html"))
                                   [(str (io/resource "two-way/b.html")) :true]]
                                  {(str (io/resource "two-way/a.html")) [nil :true]
                                   (str (io/resource "two-way/b.html"))
                                   [(str (io/resource "two-way/a.html")) :true]})))
  (testing "does not have all parents - two node seq"
    (is (not (weblink/has-all-parents? [(str (io/resource "one-way/b.html"))
                                        [(str (io/resource "one-way/a.html")) nil]]
                                       {(str (io/resource "one-way/a.html")) [nil :true]})))))


(deftest test-bidirectional-search
  (testing "one-way"
    (testing "two node seq"
      (is (= (list (str (io/resource "one-way/b.html")) (str (io/resource "one-way/c.html")))
             (first (weblink/bidirectional-search (str (io/resource "one-way/b.html"))
                                                  (str (io/resource "one-way/c.html")))))))
    (testing "three node seq"
      ; TODO Failing because it's one-directional, and we only check from the middle element
      ; whether an path has all links from start->end
      (is (= (list (str (io/resource "one-way/a.html")) (str (io/resource "one-way/b.html"))
                   (str (io/resource "one-way/c.html")))
             (first (weblink/bidirectional-search (str (io/resource "one-way/a.html"))
                                                  (str (io/resource "one-way/c.html"))))))))
  (testing "two-way"
    (testing "two node seq"
      (is (= (list (str (io/resource "two-way/b.html")) (str (io/resource "two-way/c.html")))
             (first (weblink/bidirectional-search (str (io/resource "two-way/b.html"))
                                                  (str (io/resource "two-way/c.html"))))))
      (is (= (list (str (io/resource "two-way/c.html")) (str (io/resource "two-way/b.html")))
             (first (weblink/bidirectional-search (str (io/resource "two-way/c.html"))
                                                  (str (io/resource "two-way/b.html"))))))
      (is (= (list (str (io/resource "two-way/a.html")) (str (io/resource "two-way/b.html")))
             (first (weblink/bidirectional-search (str (io/resource "two-way/a.html"))
                                                  (str (io/resource "two-way/b.html"))))))
      (is (= (list (str (io/resource "two-way/b.html")) (str (io/resource "two-way/a.html")))
             (first (weblink/bidirectional-search (str (io/resource "two-way/b.html"))
                                                  (str (io/resource "two-way/a.html")))))))
    (testing "three node seq"
      (is (= (list (str (io/resource "two-way/c.html")) (str (io/resource "two-way/b.html"))
                   (str (io/resource "two-way/a.html")))
             (first (weblink/bidirectional-search (str (io/resource "two-way/a.html"))
                                                  (str (io/resource "two-way/c.html"))))))
      (is (= (list (str (io/resource "two-way/a.html")) (str (io/resource "two-way/b.html"))
              (str (io/resource "two-way/c.html")))
             (first (weblink/bidirectional-search (str (io/resource "two-way/c.html"))
                                                  (str (io/resource "two-way/a.html"))))))))
  (testing "no path"
    (testing "two node seq"
      (is (= (list (str (io/resource "no-path/a.html")) (str (io/resource "no-path/b.html")))
             (first (weblink/bidirectional-search (str (io/resource "no-path/a.html"))
                                                  (str (io/resource "no-path/b.html"))))))
      (is (= (list (str (io/resource "no-path/c.html")) (str (io/resource "no-path/b.html")))
             (first (weblink/bidirectional-search (str (io/resource "no-path/c.html"))
                                                  (str (io/resource "no-path/b.html")))))))
    (testing "three node seq"
      (is (nil? (first (weblink/bidirectional-search (str (io/resource "no-path/a.html"))
                                                     (str (io/resource "no-path/c.html")))))))))

