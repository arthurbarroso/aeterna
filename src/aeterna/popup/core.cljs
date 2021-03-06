(ns aeterna.popup.core
  (:require [cljs.core.async :refer [go <!]]
            [chromex.ext.runtime :as runtime]
            [aeterna.popup.ui :as ui]
            [aeterna.config :as config]))

(defn extract-dates
  "Expects the `fetch-all` message's response. Accesses it's `data`
  property, makes it into a `clojure` map and then extracts it's keys"
  [msg-data]
  (-> msg-data
      .-data
      js->clj
      keys))

(defn show-dates [msg-data]
  (-> msg-data
      extract-dates
      ui/create-date-list))

(defn fetch-all-highlights! []
  (runtime/send-message
   (:extension-id config/config)
   "fetch-all"))

(defn init! []
  (.log js/console "popup")
  (go (let [[msg] (<! (fetch-all-highlights!))]
        (.log js/console "message: " msg)
        (show-dates msg))))
