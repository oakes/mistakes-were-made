(ns mistakes-were-made.core
  (:require [schema.core :refer [maybe either Any Str Int Keyword Bool
                                 #?(:clj [defn with-fn-validation])]])
  #?(:cljs (:require-macros [schema.core :refer [defn with-fn-validation]])))

(defn init! [])

(defn init-debug! []
  (.log js/console (with-out-str (time (with-fn-validation (init!))))))
