(ns lamb.background.selection
  (:require [lamb.background.storage :as storage]))

(defn make-node [text-block]
  {(random-uuid) text-block})

(defn append-to-highlights [todays-highlights highlight-to-append]
  (merge todays-highlights highlight-to-append))

(defn parse-text [selection-text]
  (-> selection-text
      js->clj
      flatten
      first
      make-node))

(defn get-date []
  (let [date (js/Date.)]
    (str (.getMonth date) "-" (.getDate date) "-" (.getFullYear date))))

(defn handle-today-save! [{:keys [current-highlights
                                  selection-text
                                  current-date]}]
  (let [parsed-text (parse-text selection-text)]
    (if (empty? (js->clj current-highlights))
      (storage/set-data! current-date parsed-text)
      (storage/set-data! current-date
                         (append-to-highlights
                          current-highlights
                          parsed-text)))))
