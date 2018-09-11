(ns async.sqlite.core
  #?(:clj
     (:require
      [async-error.core]
      [async.core]
      [clojure.core.async])))


#?(:clj
   (defmacro transaction
     [db predicate? commit-handler rollback-handler & body]
     `(async-error.core/go-try
       (-> (async.sqlite.core/begin-transaction ~db)
           (async.core/<?_ ~@body)
           (async.core/<?)
           (#(if (~predicate? %)
               (do (async.sqlite.core/commit-transaction ~db)
                   (~commit-handler %))
               (do (async.sqlite.core/rollback-transaction ~db)
                   (~rollback-handler %))))))))

