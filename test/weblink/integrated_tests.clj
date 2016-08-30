(ns weblink.integrated-tests
  (:require  [clojure.test :refer :all]
             [weblink.weblink :as weblink]))


(deftest test-wiki-pages
  (testing "ind_day -> fireworks"
    (is (= '("https://en.wikipedia.org/wiki/Independence_Day_(United_States)" "https://en.wikipedia.org/wiki/Fireworks")
           (weblink/bidirectional-search weblink/ind_day weblink/fireworks))))
  (testing "ind_day -> model_rocket"
    (is (= '("https://en.wikipedia.org/wiki/Model_rocket" "https://en.wikipedia.org/wiki/Model_rocket#Model_rocket_recovery_methods" "https://en.wikipedia.org/wiki/National_Association_of_Rocketry" "https://en.wikipedia.org/wiki/United_States" "https://en.wikipedia.org/wiki/Independence_Day_(United_States)")
           (weblink/bidirectional-search weblink/ind_day weblink/model_rocket))))
  (testing "ind_day -> Quality_of_life"
    (is (= '("https://en.wikipedia.org/wiki/Quality_of_life"
             "https://en.wikipedia.org/wiki/Standard_of_living"
             "https://en.wikipedia.org/wiki/United_States"
             "https://en.wikipedia.org/wiki/Independence_Day_(United_States)")
           (weblink/bidirectional-search weblink/ind_day weblink/quality-of-life)))))
