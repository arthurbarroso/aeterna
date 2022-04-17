(ns aeterna.popup.core
  (:require [cljs.core.async :refer [go <!]]
            [chromex.ext.runtime :as runtime]))

(defn init! []
  (.log js/console "popup")
  (go (let [[msg] (<! (runtime/send-message
                       "fhdfoojfkpncfhhckochchojlddplhmg"
                       "fetch-all"))]
        (.log js/console "message: " msg)))
  (.log js/console "POPUP: init"))
