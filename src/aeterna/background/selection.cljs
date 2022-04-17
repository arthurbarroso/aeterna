(ns aeterna.background.selection
  (:require [aeterna.background.storage :as storage]
            [cljs.core.async :refer [go <!]]))

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

(defn handle-highlights-fetching! [send-response]
  (go (let [data (<! (storage/get-all-data!))]
        (.log js/console "handled-highlight: " data)
        (send-response (clj->js {:data data})))))
