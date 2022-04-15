(ns lamb.background.selection
  (:require [lamb.background.storage :as storage]))

(defn make-node [text-block url]
  {(random-uuid) {:contents text-block
                  :url url}})

(defn append-to-highlights [todays-highlights highlight-to-append]
  (merge todays-highlights highlight-to-append))

(defn parse-text [selection-text url]
  (-> selection-text
      js->clj
      flatten
      first
      (make-node url)))

(defn get-date []
  (let [date (js/Date.)]
    (str (.getMonth date) "-" (.getDate date) "-" (.getFullYear date))))

(defn handle-today-save! [{:keys [current-highlights
                                  selection-text
                                  current-date
                                  url]}]
  (let [parsed-text (parse-text selection-text url)]
    (if (empty? (js->clj current-highlights))
      (storage/set-data! current-date parsed-text)
      (storage/set-data! current-date
                         (append-to-highlights
                          current-highlights
                          parsed-text)))))
