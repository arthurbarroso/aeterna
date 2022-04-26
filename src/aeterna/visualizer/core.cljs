(ns aeterna.visualizer.core
  (:require [cljs.core.async :refer [go-loop <! >! chan go]]
            [chromex.chrome-event-channel :as ec]
            [chromex.ext.runtime :as runtime]))

(defn handle-set-data! [msg-data]
  (cljs.pprint/pprint (js->clj msg-data)))
  ;; (doall (map #(cljs.pprint/pprint {:item  %}) (vals msg-data))))

(defn handle-popup-call [event-args]
  (let [[edata _sender send-response] event-args
        event-data (js->clj edata)
        event-type (get event-data "action")
        msg-data (get event-data "data")]
    (case event-type
      "set-data" (handle-set-data! msg-data)
      (.log js/console "couldn't detect type of event..."))))

(defn process-chrome-event [event]
  (let [[event-id event-args] event]
    (.log js/console "[visualizer]: event id:" event-id)
    (case event-id
      ::runtime/on-message (handle-popup-call event-args)
      nil)))

(defn run-chrome-event-loop! [chrome-event-channel]
  (go-loop []
    (when-some [event (<! chrome-event-channel)]
      (process-chrome-event event)
      (recur))))

(defn init! []
  (.log js/console "eiiita..")
  (let [chrome-event-channel (ec/make-chrome-event-channel (chan))]
    (runtime/tap-all-events chrome-event-channel)
    (run-chrome-event-loop! chrome-event-channel)))
