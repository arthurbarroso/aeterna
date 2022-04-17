(ns lamb.content-script
  (:require [lamb.content-script.core :as core]
            [chromex.support :refer [runonce]]))

(runonce
 (core/init!))
