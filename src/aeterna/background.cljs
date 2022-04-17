(ns aeterna.background
  (:require [aeterna.background.core :as core]
            [chromex.support :as sup]))

(sup/runonce
 (core/init!))
