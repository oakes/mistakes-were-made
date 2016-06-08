(set-env!
  :source-paths #{"src"}
  :dependencies '[[adzerk/boot-cljs "1.7.228-1" :scope "test"]
                  ; project deps
                  [org.clojure/clojure "1.8.0"]
                  [org.clojure/clojurescript "1.8.51"]
                  [prismatic/schema "0.4.3"]])

(require
  '[adzerk.boot-cljs :refer [cljs]])

(deftask run-repl []
  (repl :init-ns 'mistakes-were-made.core))

(deftask build []
  (comp (cljs :optimizations :advanced) (target)))
