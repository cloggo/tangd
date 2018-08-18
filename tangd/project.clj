(defproject tangd "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"

  :min-lein-version "2.7.1"

  :dependencies [[org.clojure/clojure "1.9.0"]
                 [binaryage/oops "0.6.2"]
                 [org.clojure/clojurescript "1.10.339"]]

  :plugins [[lein-cljsbuild "1.1.7" :exclusions [[org.clojure/clojure]]]
            [lein-figwheel "0.5.16"]]

  :source-paths ["src"]

  :clean-targets ["server.js"
                  "target"]

  :cljsbuild {
              :builds [{:id "dev"
                        :source-paths ["src"]
                        :figwheel false
                        :compiler {
                                   :main tangd.core
                                   :asset-path "target/js/compiled/dev"
                                   :output-to "target/js/compiled/tangd.js"
                                   :output-dir "target/js/compiled/dev"
                                   :target :nodejs
                                   :optimizations :none
                                   :source-map-timestamp true}}
                       {:id "prod"
                        :source-paths ["src"]
                        :compiler {
                                   :output-to "server.js"
                                   :output-dir "target/js/compiled/prod"
                                   :target :nodejs
                                   :optimizations :simple}}]}

  :profiles {:dev {:dependencies [[figwheel-sidecar "0.5.16"]
                                  [cider/piggieback "0.3.8"]]
                   :source-paths ["src" "dev"]
                   :repl-options {:nrepl-middleware [cider.piggieback/wrap-cljs-repl]
                                  :host "0.0.0.0"
                                  :port 4001}}})
