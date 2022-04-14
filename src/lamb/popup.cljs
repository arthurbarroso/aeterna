(ns lamb.popup
  (:require [lamb.popup.core :as core]
            [chromex.support :refer [runonce]]))

(runonce
 (core/init!))
