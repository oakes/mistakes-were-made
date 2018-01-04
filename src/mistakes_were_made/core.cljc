(ns mistakes-were-made.core
  (:require [clojure.string :as str]
            [clojure.spec.alpha :as s :refer [fdef]]))

(def atom? #(instance? #?(:clj clojure.lang.Atom :cljs cljs.core/Atom) %))

(s/def ::text string?)
(s/def ::cursor-position (s/tuple integer? integer?))
(s/def ::original-cursor-position ::cursor-position)
(s/def ::state (s/keys :req-un [::cursor-position ::text] :opt-un [::original-cursor-position]))
(s/def ::states (s/coll-of ::staate))
(s/def ::current-state integer?)
(s/def ::history (s/keys :req-un [::current-state ::states]))

(fdef create-edit-history
  :args (s/cat)
  :ret ::history)

(defn create-edit-history []
  (atom {:current-state -1 :states []}))

(fdef update-edit-history!
  :args (s/cat :*edit-history atom? :state ::state)
  :ret (s/nilable ::history))

(defn update-edit-history!
  "Updates the edit history atom."
  [*edit-history state]
  (let [{:keys [current-state states limit]} @*edit-history
        old-state (get states current-state)
        old-cursor-position (or (first (:original-cursor-position old-state)) 0)
        new-cursor-position (first (:cursor-position state))
        new-cursor-change (- new-cursor-position old-cursor-position)
        state (assoc state :original-cursor-position (:cursor-position state))]
    (when-not (= (:text old-state) (:text state))
      (swap! *edit-history
        (fn [edit-history]
          (let [new-current-state (if (or (<= current-state 1) (not= new-cursor-change 1))
                                    (inc current-state)
                                    current-state)
                new-states (subvec states 0 new-current-state)
                new-states (conj new-states state)]
            (if (and limit (> (count new-states) limit))
              (assoc edit-history
                :current-state (dec new-current-state)
                :states (subvec new-states 1))
              (assoc edit-history
                :current-state new-current-state
                :states new-states))))))))

(fdef update-cursor-position!
  :args (s/cat :*edit-history atom? :cursor-position ::cursor-position)
  :ret (s/nilable ::history))

(defn update-cursor-position!
  "Updates only the cursor position."
  [*edit-history cursor-position]
  (let [{:keys [current-state]} @*edit-history]
    (when (>= current-state 0)
      (swap! *edit-history assoc-in [:states current-state :cursor-position] cursor-position))))

(fdef get-current-state
  :args (s/cat :*edit-history atom?)
  :ret (s/nilable ::state))

(defn get-current-state
  "Returns the current state from edit-history, or nil if there is none."
  [*edit-history]
  (let [{:keys [current-state states]} @*edit-history]
    (get states current-state)))

(fdef get-previous-state
  :args (s/cat :*edit-history atom?)
  :ret (s/nilable ::state))

(defn get-previous-state
  "Returns the previous state from edit-history, or nil if there is none."
  [*edit-history]
  (let [{:keys [current-state states]} @*edit-history]
    (get states (dec current-state))))

(fdef get-next-state
  :args (s/cat :*edit-history atom?)
  :ret (s/nilable ::state))

(defn get-next-state
  "Returns the next state from edit-history, or nil if there is none."
  [*edit-history]
  (let [{:keys [current-state states]} @*edit-history]
    (get states (inc current-state))))

(fdef undo!
  :args (s/cat :*edit-history atom?)
  :ret (s/nilable ::history))

(defn undo!
  "Changes the current state and returns the previous state from edit-history, or nil if there is none."
  [*edit-history]
  (when-let [state (get-previous-state *edit-history)]
    (swap! *edit-history update :current-state dec)
    state))

(fdef redo!
  :args (s/cat :*edit-history atom?)
  :ret (s/nilable ::history))

(defn redo!
  "Changes the current state and returns the next state from edit-history, or nil if there is none."
  [*edit-history]
  (when-let [state (get-next-state *edit-history)]
    (swap! *edit-history update :current-state inc)
    state))

(fdef can-undo?
  :args (s/cat :*edit-history atom?)
  :ret boolean?)

(defn can-undo?
  "Returns true if we can undo."
  [*edit-history]
  (some? (get-previous-state *edit-history)))

(fdef can-redo?
  :args (s/cat :*edit-history atom?)
  :ret boolean?)

(defn can-redo?
  "Returns true if we can redo."
  [*edit-history]
  (some? (get-next-state *edit-history)))

