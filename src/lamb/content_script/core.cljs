(ns lamb.content-script.core
  (:require [cljs.core.async :refer [go-loop <!]]
            [chromex.protocols.chrome-port :refer [post-message!]]
            [chromex.ext.runtime :as runtime]))

(defn process-message! [message]
  (.log js/console "CONTENT SCRIPT: got message:" message))

(defn run-message-loop! [message-channel]
  (.log js/console "CONTENT SCRIPT: starting message loop...!")
  (go-loop []
    (.log js/console "entered go loop")
    (when-some [message (<! message-channel)]
      (.log js/console "run go loop")
      (process-message! message)
      (recur))
    (.log js/console "CONTENT SCRIPT: ending go loop...")))

(defn connect-to-background-page! []
  (let [background-port (runtime/connect)]
    (post-message! background-port "hello from CONTENT SCRIPT!")
    (run-message-loop! background-port)
    (post-message! background-port "hello from CONTENT SCRIPT!")))

(defn init! [])
