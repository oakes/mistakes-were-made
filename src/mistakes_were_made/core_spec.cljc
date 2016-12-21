(ns mistakes-were-made.core-spec
  (:require [mistakes-were-made.core :as c]
            [clojure.spec :as s :refer [fdef]]))

(def atom? #(instance? #?(:clj clojure.lang.Atom :cljs cljs.core/Atom) %))

(s/def ::text string?)
(s/def ::cursor-position (s/tuple integer? integer?))
(s/def ::original-cursor-position ::cursor-position)
(s/def ::state (s/keys :req-un [::cursor-position ::text] :opt-un [::original-cursor-position]))
(s/def ::states (s/coll-of ::staate))
(s/def ::current-state integer?)
(s/def ::history (s/keys :req-un [::current-state ::states]))

(fdef c/create-edit-history
  :args (s/cat)
  :ret ::history)

(fdef c/update-edit-history!
  :args (s/cat :edit-history atom? :state ::state)
  :ret (s/nilable ::history))

(fdef c/update-cursor-position!
  :args (s/cat :edit-history atom? :cursor-position ::cursor-position)
  :ret (s/nilable ::history))

(fdef c/get-current-state
  :args (s/cat :edit-history atom?)
  :ret (s/nilable ::state))

(fdef c/get-previous-state
  :args (s/cat :edit-history atom?)
  :ret (s/nilable ::state))

(fdef c/get-next-state
  :args (s/cat :edit-history atom?)
  :ret (s/nilable ::state))

(fdef c/undo!
  :args (s/cat :edit-history atom?)
  :ret (s/nilable ::history))

(fdef c/redo!
  :args (s/cat :edit-history atom?)
  :ret (s/nilable ::history))

(fdef c/can-undo?
  :args (s/cat :edit-history atom?)
  :ret boolean?)

(fdef c/can-redo?
  :args (s/cat :edit-history atom?)
  :ret boolean?)

