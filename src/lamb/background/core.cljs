(ns lamb.background.core
  (:require [goog.string :as gstring]
            [goog.string.format]
            [cljs.core.async :refer [go-loop <! >! chan go]]
            [chromex.logging :refer-macros [log info warn error
                                            group group-end]]
            [chromex.chrome-event-channel :as ec]
            [chromex.protocols.chrome-port :as cp]
            [chromex.ext.tabs :as tabs]
            [chromex.ext.runtime :as runtime]
            [lamb.background.storage :as storage]
            [chromex.ext.context-menus :as cm]
            [clojure.string :as string]))

(def clients (atom []))

(defn add-client! [client]
  (log "BACKGROUND: client connected " (cp/get-sender client))
  (swap! clients conj client))

(defn remove-client! [client]
  (log "BACKGROUND: client disconnected " (cp/get-sender client))
  (let [remove-item (fn [coll item] (remove #(identical? item %) coll))]
    (swap! clients remove-item client)))

(defn run-client-message-loop! [client]
  (log "BACKGROUND: starting event loop for client: " (cp/get-sender client))
  (go-loop []
    (when-some [message (<! client)]
      (log "BACKGROUND: got client message: " message "from " (cp/get-sender client))
      (recur))
    (log "BACKGROUND: leaving event loop for client: " (cp/get-sender client))
    (remove-client! client)))

(defn handle-client-connection! [client]
  (add-client! client)
  (cp/post-message! client "hello from BACKGROUND PAGE!")
  (run-client-message-loop! client))

(defn tell-clients-about-new-tab! []
  (doseq [client @clients]
    (cp/post-message! client "a new tab was created")))

(defn get-caller-tab! []
  (let [query (clj->js {:active true :lastFocusedWindow true})
        return (chan)]
    (go
      (while true
        (let [tabs (<! (tabs/query query))]
          (>! return (-> tabs first first .-id)))))

    return))

(defn split-selection-text [text]
  (string/split text #"\r?\n" -1))

(defn make-node [text]
  {:id (random-uuid)
   :text text})

(defn rebuild-text [text-items]
  (reduce #(str %1 "\n" %2) "" text-items))

(defn execute-script! [tab-id]
  (let [out-chan (chan)
        script (clj->js {:code "window.getSelection().toString()"})]
    (go
      (let [result (<! (tabs/execute-script tab-id script))]
        (>! out-chan result)))
    out-chan))

(defn get-selection []
  (go
    (let [a (<! (get-caller-tab!))
          b (<! (execute-script! a))
          res (split-selection-text (first (flatten (js->clj b))))]
       ;; (log a)
      (.log js/console res)
      (.log js/console (rebuild-text res)))))

(defn handle-context-click [event-args]
  (let [menu-id (-> event-args first .-menuItemId)]
    (if (= menu-id "my-exit-ev")
      (get-selection)
      nil)))

(defn process-chrome-event [event-num event]
  (log (gstring/format "BACKGROUND: got chrome event (%05d)" event-num)
       event)
  (log (first event))
  (let [[event-id event-args] event]
    (case event-id
      ::runtime/on-connect (apply handle-client-connection! event-args)
      ::tabs/on-created (tell-clients-about-new-tab!)
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
  (storage/test-storage!)
  (log ::cm/on-cliked)
  (cm/create (clj->js {:title "my-ext-dev"
                       :id "my-exit-ev"
                       :contexts ["all"]}))
  (boot-chrome-event-loop!))
