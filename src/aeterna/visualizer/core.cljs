(ns aeterna.visualizer.core
  (:require [cljs.core.async :refer [go-loop <! >! chan go]]
            [chromex.chrome-event-channel :as ec]
            [chromex.ext.runtime :as runtime]))

(defn handle-set-data! [msg-data]
  (.log js/console msg-data))

(defn handle-popup-call [event-args]
  (let [[event-type _sender send-response] event-args]
    (case event-type
      "set-data" (handle-set-data! event-args)
      (.log js/console "couldn't detect type of event..."))))

(defn process-chrome-event [event]
  (.log js/console "event on background visualizerr: " event)
  (let [[event-id event-args] event]
    (.log js/console "event on background, id: " event-id)
    (case event-id
      ::runtime/on-message (handle-popup-call event-args)
      nil)))

(defn run-chrome-event-loop! [chrome-event-channel]
  (go-loop []
    (when-some [event (<! chrome-event-channel)]
      (process-chrome-event event)
      (recur))))

(defn init! []
  (let [chrome-event-channel (ec/make-chrome-event-channel (chan))]
    (runtime/tap-all-events chrome-event-channel)
    (run-chrome-event-loop! chrome-event-channel)))
