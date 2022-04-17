(ns aeterna.popup
  (:require [aeterna.popup.core :as core]
            [chromex.support :refer [runonce]]))

(runonce
 (core/init!))
