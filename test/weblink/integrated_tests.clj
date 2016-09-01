(ns weblink.integrated-tests
  (:require  [clojure.test :refer :all]
             [weblink.weblink :as weblink]))

;; Test Data
(def yegge-web "http://steve-yegge.blogspot.com")
(def jython "http://www.jython.org")
(def jython-book "http://www.jythonbook.org")
(def ind_day "https://en.wikipedia.org/wiki/Independence_Day_(United_States)")
(def us "https://en.wikipedia.org/wiki/United_States")
(def fireworks "https://en.wikipedia.org/wiki/Fireworks")
(def model_rocket "https://en.wikipedia.org/wiki/Model_rocket")
(def quality-of-life "https://en.wikipedia.org/wiki/Quality_of_life")


(deftest test-wiki-pages
  (testing "ind_day -> fireworks"
    (is (= '("https://en.wikipedia.org/wiki/Independence_Day_(United_States)" "https://en.wikipedia.org/wiki/Fireworks")
           (weblink/bidirectional-search ind_day fireworks))))
  (testing "ind_day -> model_rocket"
    (is (= '("https://en.wikipedia.org/wiki/Model_rocket" "https://en.wikipedia.org/wiki/Model_rocket#Model_rocket_recovery_methods" "https://en.wikipedia.org/wiki/National_Association_of_Rocketry" "https://en.wikipedia.org/wiki/United_States" "https://en.wikipedia.org/wiki/Independence_Day_(United_States)")
           (weblink/bidirectional-search ind_day model_rocket))))
  (testing "ind_day -> Quality_of_life"
    (is (= '("https://en.wikipedia.org/wiki/Quality_of_life"
             "https://en.wikipedia.org/wiki/Standard_of_living"
             "https://en.wikipedia.org/wiki/United_States"
             "https://en.wikipedia.org/wiki/Independence_Day_(United_States)")
           (weblink/bidirectional-search ind_day quality-of-life)))))
