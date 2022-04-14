(ns lamb.background.core
  (:require [goog.string :as gstring]
            [goog.string.format]
            [cljs.core.async :refer [go-loop <! >! chan go]]
            [chromex.logging :refer-macros [log]]
            [chromex.chrome-event-channel :as ec]
            [chromex.ext.tabs :as tabs]
            [chromex.ext.runtime :as runtime]
            [lamb.background.storage :as storage]
            [lamb.background.selection :as selection]
            [chromex.ext.context-menus :as cm]))

(defn get-caller-tab! []
  (let [query (clj->js {:active true :lastFocusedWindow true})
        output (chan)]
    (go
      (while true
        (let [tabs (<! (tabs/query query))]
          (>! output (-> tabs first first .-id)))))
    output))

(defn execute-selection! [tab-id]
  (let [out-chan (chan)
        script (clj->js {:code "window.getSelection().toString()"})]
    (go
      (let [result (<! (tabs/execute-script tab-id script))]
        (>! out-chan result)))
    out-chan))

(defn save-selection!! []
  (go (let [tab-id (<! (get-caller-tab!))
            selection-text (<! (execute-selection! tab-id))
            today (selection/get-date)
            current-day (<! (storage/get-data! today))
            current-highlights (get (js->clj current-day) today)]
        (selection/handle-today-save! {:current-highlights (js->clj current-highlights)
                                       :selection-text selection-text
                                       :current-date today}))))

(defn handle-context-click [event-args]
  (let [menu-id (-> event-args first .-menuItemId)]
    (if (= menu-id "my-exit-ev")
      (save-selection!!)
      nil)))

(defn process-chrome-event [event-num event]
  (log (gstring/format "BACKGROUND: got chrome event (%05d)" event-num)
       event)
  (log (first event))
  (let [[event-id event-args] event]
    (case event-id
      ::cm/on-clicked (handle-context-click event-args)
      nil)))

(defn run-chrome-event-loop! [chrome-event-channel]
  (log "BACKGROUND: starting main event loop...")
  (go-loop [event-num 1]
    (when-some [event (<! chrome-event-channel)]
      (process-chrome-event event-num event)
      (recur (inc event-num)))
    (log "BACKGROUND: leaving main event loop")))

(defn boot-chrome-event-loop! []
  (let [chrome-event-channel (ec/make-chrome-event-channel (chan))]
    (tabs/tap-all-events chrome-event-channel)
    (cm/tap-on-clicked-events chrome-event-channel)
    (runtime/tap-all-events chrome-event-channel)
    (run-chrome-event-loop! chrome-event-channel)))

(defn init! []
  (log "BACKGROUND: init")
  (cm/create (clj->js {:title "my-ext-dev"
                       :id "my-exit-ev"
                       :contexts ["all"]}))
  (boot-chrome-event-loop!))
