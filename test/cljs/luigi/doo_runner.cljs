(ns luigi.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [luigi.core-test]))

(doo-tests 'luigi.core-test)
