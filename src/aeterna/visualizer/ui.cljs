(ns aeterna.visualizer.ui
  (:require [clojure.walk :as walk]))

(defn mount-title [date]
  (let [title-container (.getElementById js/document "viewing-date")]
    (set! (.-innerText title-container) date)))

(defn create-quote-element
  [quote list-container]
  (let [{:keys [contents url]} (walk/keywordize-keys (first (vals quote)))
        uuid (first (keys quote))
        li (.createElement js/document "li")
        quote-content (.createElement js/document "p")
        quote-site (.createElement js/document "p")
        site-link (.createElement js/document "a")]
    (.setAttribute li "id" uuid)
    (set! (.-innerText quote-site) "From: ")
    (set! (.-innerText quote-content) contents)
    (set! (.-innerText site-link) url)
    (.appendChild quote-site site-link)
    (.appendChild li quote-site)
    (.appendChild li quote-content)
    (.appendChild list-container li)))

(defn mount-visualization [quotes]
  (let [list-container (.getElementById js/document "aeterna-contents")]
    (doseq [quote quotes]
      (create-quote-element quote list-container))))
