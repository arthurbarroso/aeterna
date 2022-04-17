(ns aeterna.content-script
  (:require [aeterna.content-script.core :as core]
            [chromex.support :refer [runonce]]))

(runonce
 (core/init!))
