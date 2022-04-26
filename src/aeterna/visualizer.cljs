(ns aeterna.visualizer
  (:require [aeterna.visualizer.core :as core]
            [chromex.support :refer [runonce]]))

(runonce
 (core/init!))
