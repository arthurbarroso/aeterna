(ns aeterna.popup.ui
  (:require [chromex.ext.runtime :as runtime]
            [aeterna.config :as config]))

(defn check-date-contents [date]
  (.log js/console "-> check date contents")
  (runtime/send-message
   (:extension-id config/config)
   (clj->js {:action "open-contents" :data date})))

(defn create-date-element
  "Creates an `li` element containing a date. Appends it as a
  list-container's child."
  [date list-container]
  (let [li (.createElement js/document "li")]
    (.setAttribute li "id" date)
    (set! (.-innerText li) date)
    (.addEventListener li "click" #(check-date-contents date))
    (.appendChild list-container li)))

(defn create-date-list [dates]
  (let [list-container (.getElementById js/document "aeterna-dates")]
    (doseq [date dates]
      (create-date-element date list-container))))
