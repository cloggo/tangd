(defproject tangd "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"

  :min-lein-version "2.7.1"

  :dependencies [[org.clojure/clojure "1.9.0"]
                 [com.cognitect/transit-cljs "0.8.256"]
                 [org.clojure/core.async "0.4.474"]
                 [org.clojars.akiel/async-error "0.3"]
                 [binaryage/devtools "0.9.10"]
                 [org.clojars.cloggo/cljs-async "0.1.2"]
                 [org.clojars.cloggo/sqlite "0.1.1"]
                 [org.clojars.cloggo/jose "0.1.1"]
                 [org.clojars.cloggo/interop "0.1.0"]
                 [org.clojars.cloggo/restify "0.1.3"]
                 [org.clojars.cloggo/async-restify "0.1.0"]
                 [org.clojars.cloggo/async-sqlite "0.1.3"]
                 #_[funcool/promesa "1.9.0"]
                 #_[re-frame "0.10.6"]
                 [binaryage/oops "0.6.2"]
                 [org.clojure/clojurescript "1.10.339"]]

  :plugins [[lein-cljsbuild "1.1.7" :exclusions [[org.clojure/clojure]]]
            [lein-doo "0.1.10"]
            [lein-figwheel "0.5.16"]]

  :source-paths ["src"]

  :clean-targets ["server.js"
                  "target"]

  ;; https://github.com/clojure/clojurescript-site/blob/master/content/reference/compiler-options.adoc
  :cljsbuild {
              :builds [{:id "dev"
                        :source-paths ["src" "private"]
                        :figwheel false
                        :compiler {
                                   :main app.core
                                   :closure-defines {goog.DEBUG true}
                                   ;; :asset-path "target/js/compiled/dev"
                                   :output-to "target/js/compiled/app.js"
                                   :output-dir "target/js/compiled/dev"
                                   :source-map true
                                   :target :nodejs
                                   :install-deps true
                                   :preloads [devtools.preload]
                                   :optimizations :none
                                   :source-map-timestamp true}}
                       {:id "test"
                        :source-paths ["test" "src" "private"]
                        :figwheel false
                        :compiler {
                                   :main test.core
                                   :closure-defines {goog.DEBUG true}
                                   :asset-path "target/js/compiled/test"
                                   :output-to "target/js/compiled/test.js"
                                   :output-dir "target/js/compiled/test"
                                   :install-deps true
                                   :preloads [devtools.preload]
                                   :source-map true
                                   :target :nodejs
                                   :optimizations :none
                                   :source-map-timestamp true}}
                       {:id "prod"
                        :source-paths ["src" "private"]
                        :compiler {
                                   :closure-defines {goog.DEBUG false}
                                   :output-to "index.js"
                                   :output-dir "target/js/compiled/prod"
                                   :install-deps true
                                   :target :nodejs
                                   :optimizations :advanced}}]}
  ;; no need for node.js :optimizations :advanced but it does save about 5M of memory}}]}

  :profiles {:dev {:dependencies [[figwheel-sidecar "0.5.16"]
                                  [cider/piggieback "0.3.8"]]
                   :source-paths ["src" "dev" "private"]
                   :repl-options {:nrepl-middleware [cider.piggieback/wrap-cljs-repl]
                                  :host "0.0.0.0"
                                  :server-port 3449
                                  :nrepl-port 4001}}})

;; (def npm-dev
;;   (merge {} npm-prod))


