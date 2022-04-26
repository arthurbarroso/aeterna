(ns aeterna.visualizer.ui)

(defn mount-title [date]
  (let [title-container (.getElementById js/document "viewing-date")]
    (set! (.-innerText title-container) date)))

(defn mount-visualization [])

(defn create-date-element
  "Creates an `li` element containing a date. Appends it as a
  list-container's child."
  [date list-container]
  (let [li (.createElement js/document "li")]
    (.setAttribute li "id" date)
    (set! (.-innerText li) date)
    (.appendChild list-container li)))

(defn create-date-list [dates]
  (let [list-container (.getElementById js/document "aeterna-dates")]
    (doseq [date dates]
      (create-date-element date list-container))))
