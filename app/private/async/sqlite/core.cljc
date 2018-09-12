(ns async.sqlite.core
  #?(:clj
     (:require
      #_[async-error.core]
      [async.core]
      #_[cljs.core.async]
      #_[clojure.core.async])))


#?(:clj
   (defmacro transaction
     "predicate? if fail return true"
     [db params & body]
     (let [[commit-handler rollback-handler predicate?] params
           rollback-handler (or rollback-handler
                                `(fn [err#]
                                   {:status :INTERNAL_SERVER_ERROR
                                             :error (async.core/append-error-message
                                                     err# " [Change not committed.]")}))
           predicate? (or predicate? `async.core/error?)]
       `(async.core/go-try
         (-> (async.sqlite.core/begin-transaction ~db)
             (async.core/<?_ ~@body)
             (async.core/<!)
             (#(if (~predicate? %)
                 (do (async.sqlite.core/rollback-transaction ~db)
                     (~rollback-handler %))
                 (do (async.sqlite.core/commit-transaction ~db)
                     (~commit-handler %)))))))))

