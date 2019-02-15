(defproject mistakes-were-made "1.8.1-SNAPSHOT"
  :description "An undo/redo system for Clojure and ClojureScript"
  :url "https://github.com/oakes/mistakes-were-made"
  :license {:name "Public Domain"
            :url "http://unlicense.org/UNLICENSE"}
  :plugins [[lein-tools-deps "0.4.3"]]
  :middleware [lein-tools-deps.plugin/resolve-dependencies-with-deps-edn]
  :lein-tools-deps/config {:config-files [:install :user :project]}
  :profiles {:dev {:main mistakes-were-made.core}})
