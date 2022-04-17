(ns aeterna.popup.core
  (:require [cljs.core.async :refer [go <!]]
            [chromex.ext.runtime :as runtime]))

(defn extract-dates [msg-data]
  (-> msg-data
      .-data
      js->clj
      keys))

(defn create-date-element [date list-container]
  (let [li (.createElement js/document "li")]
    (.setAttribute li "id" date)
    (set! (.-innerText li) date)
    (.appendChild list-container li)))

(defn create-date-list [dates]
  (let [list-container (.getElementById js/document "aeterna-dates")]
    (doseq [date dates]
      (create-date-element date list-container))))

(defn show-dates [data]
  (-> data
      extract-dates
      create-date-list))

(defn init! []
  (.log js/console "popup")
  (go (let [[msg] (<! (runtime/send-message
                       "fhdfoojfkpncfhhckochchojlddplhmg"
                       "fetch-all"))]
        (.log js/console "message: " msg)
        (show-dates msg)))
  (.log js/console "POPUP: init"))
