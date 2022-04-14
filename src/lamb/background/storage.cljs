(ns lamb.background.storage
  (:require [cljs.core.async :refer [go <! >! chan]]
            [chromex.logging :refer-macros [log]]
            [chromex.protocols.chrome-storage-area :as sa]
            [chromex.ext.storage :as storage]))

(defn set-data! [key data]
  (let [local-storage (storage/get-local)
        js-data (clj->js {key data})]
    (sa/set local-storage js-data)))

(defn get-all-data! []
  (let [local-storage (storage/get-local)
        output (chan)]
    (go (let [[[items] error] (<! (sa/get local-storage))]
          (if error
            (.log js/console
                  (str "fetch all error: " error))
            (>! output items))))
    output))

(defn get-data! [key]
  (let [local-storage (storage/get-local)
        output (chan)]
    (go (let [[[items] error] (<! (sa/get local-storage key))]
          (if error
            (.log js/console
                  (str "fetch " key " error: " error))
            (>! output items))))
    output))
