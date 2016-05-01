(ns mistakes-were-made.core
  (:require [clojure.string :as str]
            [schema.core :refer [maybe either Any Str Int Keyword Bool]
             #?@(:clj [:as s])])
  #?(:cljs (:require-macros [schema.core :as s])))

(s/defn create-edit-history []
  (atom {:current-state -1 :states []}))

(s/defn update-edit-history!
  "Updates the edit history atom."
  [edit-history :- Any
   state :- {Keyword Any}]
  (let [{:keys [current-state states]} @edit-history
        old-state (get states current-state)
        old-cursor-position (or (first (:cursor-position old-state)) 0)
        new-cursor-position (first (:cursor-position state))
        new-cursor-change (- new-cursor-position old-cursor-position)]
    (when-not (= (:text old-state) (:text state))
      ; if the last edit wasn't a single character after the previous edit, make it a separate undoable edit
      (when (or (<= current-state 1)
                (not= new-cursor-change 1))
        (swap! edit-history update-in [:current-state] inc))
      (swap! edit-history update-in [:states] subvec 0 (:current-state @edit-history))
      (swap! edit-history update-in [:states] conj state))))

(s/defn update-cursor-position!
  "Updates only the cursor position."
  [edit-history :- Any
   cursor-position :- [Int]]
  (let [{:keys [current-state]} @edit-history]
    (when (>= current-state 0)
      (swap! edit-history assoc-in [:states current-state :cursor-position] cursor-position))))

(s/defn get-current-state :- (maybe {Keyword Any})
  "Returns the current state from edit-history, or nil if there is none."
  [edit-history :- Any]
  (let [{:keys [current-state states]} @edit-history]
    (get states current-state)))

(s/defn get-previous-state :- (maybe {Keyword Any})
  "Returns the previous state from edit-history, or nil if there is none."
  [edit-history :- Any]
  (let [{:keys [current-state states]} @edit-history]
    (get states (dec current-state))))

(s/defn get-next-state :- (maybe {Keyword Any})
  "Returns the next state from edit-history, or nil if there is none."
  [edit-history :- Any]
  (let [{:keys [current-state states]} @edit-history]
    (get states (inc current-state))))

(s/defn undo! :- (maybe {Keyword Any})
  "Changes the current state and returns the previous state from edit-history, or nil if there is none."
  [edit-history :- Any]
  (when-let [state (get-previous-state edit-history)]
    (swap! edit-history update :current-state dec)
    state))

(s/defn redo! :- (maybe {Keyword Any})
  "Changes the current state and returns the next state from edit-history, or nil if there is none."
  [edit-history :- Any]
  (when-let [state (get-next-state edit-history)]
    (swap! edit-history update :current-state inc)
    state))

(s/defn can-undo? :- Bool
  "Returns true if we can undo."
  [edit-history :- Any]
  (some? (get-previous-state edit-history)))

(s/defn can-redo? :- Bool
  "Returns true if we can redo."
  [edit-history :- Any]
  (some? (get-next-state edit-history)))
