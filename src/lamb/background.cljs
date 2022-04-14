(ns lamb.background
  (:require [lamb.background.core :as core]
            [chromex.support :as sup]))

(sup/runonce
 (core/init!))
