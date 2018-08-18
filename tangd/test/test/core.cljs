(ns test.core
  (:require
   [doo.runner :refer-macros [doo-tests]]
   [test.hello]))

(doo-tests 'test.hello)


