(set-env!
  :source-paths #{"src"}
  :resource-paths #{"src"}
  :dependencies '[[org.clojure/clojure "1.9.0-alpha11"]
                  [org.clojure/clojurescript "1.9.225"]]
  :repositories (conj (get-env :repositories)
                  ["clojars" {:url "https://clojars.org/repo/"
                              :username (System/getenv "CLOJARS_USER")
                              :password (System/getenv "CLOJARS_PASS")}]))

(task-options!
  pom {:project 'mistakes-were-made
       :version "1.6.5-SNAPSHOT"
       :description "An undo/redo system for Clojure and ClojureScript"
       :url "https://github.com/oakes/mistakes-were-made"
       :license {"Public Domain" "http://unlicense.org/UNLICENSE"}}
  push {:repo "clojars"})

(deftask local []
  (comp (pom) (jar) (install)))

(deftask deploy []
  (comp (pom) (jar) (push)))

