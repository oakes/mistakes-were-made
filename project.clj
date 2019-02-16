(defproject mistakes-were-made "1.8.1-SNAPSHOT"
  :description "An undo/redo system for Clojure and ClojureScript"
  :url "https://github.com/oakes/mistakes-were-made"
  :license {:name "Public Domain"
            :url "http://unlicense.org/UNLICENSE"}
  :repositories [["clojars" {:url "https://clojars.org/repo"
                             :sign-releases false}]]
  :profiles {:dev {:main mistakes-were-made.core}})
