(ns ^:figwheel-no-load luigi.dev
  (:require
    [luigi.core :as core]
    [devtools.core :as devtools]))

(devtools/install!)

(enable-console-print!)

(core/init!)
