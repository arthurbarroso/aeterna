(ns aeterna.content-script.core
  (:require [cljs.core.async :refer [go-loop <!]]
            [chromex.protocols.chrome-port :refer [post-message!]]
            [chromex.ext.runtime :as runtime]))

(defn init! [])
