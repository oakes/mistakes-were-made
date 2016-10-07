(ns mistakes-were-made.core
  (:require [clojure.string :as str]))

(defn create-edit-history []
  (atom {:current-state -1 :states []}))

(defn update-edit-history!
  "Updates the edit history atom."
  [edit-history state]
  (let [{:keys [current-state states limit]} @edit-history
        old-state (get states current-state)
        old-cursor-position (or (first (:original-cursor-position old-state)) 0)
        new-cursor-position (first (:cursor-position state))
        new-cursor-change (- new-cursor-position old-cursor-position)
        state (assoc state :original-cursor-position (:cursor-position state))]
    (when-not (= (:text old-state) (:text state))
      (swap! edit-history
        (fn [edit-history-map]
          (let [new-current-state (if (or (<= current-state 1) (not= new-cursor-change 1))
                                    (inc current-state)
                                    current-state)
                new-states (subvec states 0 new-current-state)
                new-states (conj new-states state)]
            (if (and limit (> (count new-states) limit))
              (assoc edit-history-map
                :current-state (dec new-current-state)
                :states (subvec new-states 1))
              (assoc edit-history-map
                :current-state new-current-state
                :states new-states))))))))

(defn update-cursor-position!
  "Updates only the cursor position."
  [edit-history cursor-position]
  (let [{:keys [current-state]} @edit-history]
    (when (>= current-state 0)
      (swap! edit-history assoc-in [:states current-state :cursor-position] cursor-position))))

(defn get-current-state
  "Returns the current state from edit-history, or nil if there is none."
  [edit-history]
  (let [{:keys [current-state states]} @edit-history]
    (get states current-state)))

(defn get-previous-state
  "Returns the previous state from edit-history, or nil if there is none."
  [edit-history]
  (let [{:keys [current-state states]} @edit-history]
    (get states (dec current-state))))

(defn get-next-state
  "Returns the next state from edit-history, or nil if there is none."
  [edit-history]
  (let [{:keys [current-state states]} @edit-history]
    (get states (inc current-state))))

(defn undo!
  "Changes the current state and returns the previous state from edit-history, or nil if there is none."
  [edit-history]
  (when-let [state (get-previous-state edit-history)]
    (swap! edit-history update :current-state dec)
    state))

(defn redo!
  "Changes the current state and returns the next state from edit-history, or nil if there is none."
  [edit-history]
  (when-let [state (get-next-state edit-history)]
    (swap! edit-history update :current-state inc)
    state))

(defn can-undo?
  "Returns true if we can undo."
  [edit-history]
  (some? (get-previous-state edit-history)))

(defn can-redo?
  "Returns true if we can redo."
  [edit-history]
  (some? (get-next-state edit-history)))

