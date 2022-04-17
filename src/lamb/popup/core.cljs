(ns lamb.popup.core
  (:require [cljs.core.async :refer [go-loop <!]]
            [chromex.ext.runtime :as runtime]))

(defn init! []
  (.log js/console "popup")
  (runtime/send-message "fhdfoojfkpncfhhckochchojlddplhmg" "fetch-all")
  (.log js/console "POPUP: init"))
