(ns aeterna.background.core
  (:require [cljs.core.async :refer [go-loop <! >! chan go]]
            [chromex.chrome-event-channel :as ec]
            [chromex.ext.tabs :as tabs]
            [aeterna.background.storage :as storage]
            [aeterna.background.selection :as selection]
            [chromex.ext.context-menus :as cm]
            [chromex.ext.runtime :as runtime]))

(defn get-caller-tab! []
  (let [query (clj->js {:active true :lastFocusedWindow true})
        output (chan)]
    (go
      (while true
        (let [tabs (<! (tabs/query query))
              current-tab (-> tabs first first)]
          (>! output {:tab-id (.-id current-tab)
                      :url (.-url current-tab)}))))
    output))

(defn execute-selection! [tab-id]
  (let [out-chan (chan)
        script (clj->js {:code "window.getSelection().toString()"})]
    (go
      (let [result (<! (tabs/execute-script tab-id script))]
        (>! out-chan result)))
    out-chan))

(defn save-selection! []
  (go (let [tab (<! (get-caller-tab!))
            selection-text (<! (execute-selection! (:tab-id tab)))
            today (selection/get-date)
            current-day (<! (storage/get-data! today))
            current-highlights (get (js->clj current-day) today)]
        (selection/handle-today-save! {:current-highlights (js->clj current-highlights)
                                       :selection-text selection-text
                                       :current-date today
                                       :url (:url tab)}))))

(defn handle-context-click [event-args]
  (let [menu-id (-> event-args first .-menuItemId)]
    (if (= menu-id "aeterna")
      (save-selection!)
      nil)))

(defn handle-fetch-all! [send-response]
  (do (.log js/console "fetch all event received...")
      (.log js/console send-response)
      (selection/handle-highlights-fetching! send-response)))

(defn handle-popup-call [event-args]
  (let [[event-type _sender send-response] event-args]
    (case event-type
      "fetch-all" (handle-fetch-all! send-response)
      (.log js/console "couldn't detect type of event..."))))

(defn process-chrome-event [event]
  (.log js/console "event on background: " event)
  (let [[event-id event-args] event]
    (.log js/console "event on background, id: " event-id)
    (case event-id
      ::cm/on-clicked (handle-context-click event-args)
      ::runtime/on-message (handle-popup-call event-args)
      nil)))

(defn run-chrome-event-loop! [chrome-event-channel]
  (go-loop []
    (when-some [event (<! chrome-event-channel)]
      (process-chrome-event event)
      (recur))))

(defn boot-chrome-event-loop! []
  (let [chrome-event-channel (ec/make-chrome-event-channel (chan))]
    (tabs/tap-all-events chrome-event-channel)
    (cm/tap-on-clicked-events chrome-event-channel)
    (runtime/tap-all-events chrome-event-channel)
    (run-chrome-event-loop! chrome-event-channel)))

(defn init! []
  (cm/create (clj->js {:title "aeterna - save selection"
                       :id "aeterna"
                       :contexts ["selection"]}))
  (boot-chrome-event-loop!))
