(ns lamb.background.storage
  (:require [cljs.core.async :refer [go <! chan]]
            [chromex.logging :refer-macros [log info warn error group
                                            group-end]]
            [chromex.protocols.chrome-storage-area :as sa]
            [chromex.ext.storage :as storage]))

(defn test-storage! []
  (let [local-storage (storage/get-local)
        data (clj->js {:key1 "teste"
                       :key2 "arr"})]
    (sa/set local-storage data)
    (go
      (let [[[items] error] (<! (sa/get local-storage))]
        (if error
          (error "fetch all error: " error)
          (log "fetch all: " items))))
    (go
      (let [[[items] error] (<! (sa/get local-storage "key1"))]
        (if error
          (error "fetch key1 error: " error)
          (log "fetch key1: " items))))))
