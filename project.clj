(defproject mistakes-were-made "1.5.2"
  :description "An undo/redo system for Clojure and ClojureScript"
  :url "https://github.com/oakes/mistakes-were-made"
  :license {:name "Public Domain"
            :url "http://unlicense.org/UNLICENSE"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.7.228"]
                 [prismatic/schema "0.4.3"]]
  :profiles {:uberjar {:prep-tasks ["compile" ["cljsbuild" "once"]]}}
  :javac-options ["-target" "1.6" "-source" "1.6" "-Xlint:-options"]
  :plugins [[lein-cljsbuild "1.1.2"]]
  :cljsbuild {:builds {:main {:source-paths ["src"]
                              :compiler {:output-to "resources/public/mistakes-were-made.js"
                                         :optimizations :advanced
                                         :pretty-print false}
                              :jar true}}}
  :main mistakes-were-made.core)
