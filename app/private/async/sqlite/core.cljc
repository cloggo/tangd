(ns async.sqlite.core
  #?(:clj
     (:require
      [async-error.core]
      [async.core]
      [clojure.core.async])))


#?(:clj
   (defmacro transaction
     "predicate? if fail return true"
     [db params & body]
     (let [[commit-handler rollback-handler predicate?] params
           predicate? (or predicate? 'identity)]
       `(async-error.core/go-try
         (-> (async.sqlite.core/begin-transaction ~db)
             (async.core/<?_ ~@body)
             (async-error.core/<?)
             (#(if (~predicate? %)
                 (do (async.sqlite.core/rollback-transaction ~db)
                     (~rollback-handler %))
                 (do (async.sqlite.core/commit-transaction ~db)
                     (~commit-handler %)))))))))

