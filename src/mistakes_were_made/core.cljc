(ns mistakes-were-made.core
  (:require [clojure.string :as str]
            [schema.core :refer [maybe either Any Str Int Keyword Bool]
             #?@(:clj [:as s])])
  #?(:cljs (:require-macros [schema.core :as s])))

(s/defn split-lines :- [Str]
  "Splits the string into lines."
  [s :- Str]
  (vec (.split s "\n" -1)))

(s/defn position->row-col :- [Int]
  "Converts a position to a row and column number."
  [text :- Str
   position :- Int]
  (let [text (subs text 0 position)
        last-newline (.lastIndexOf text (str \newline))
        row (count (re-seq #"\n" text))
        col (if (>= last-newline 0)
              (- position last-newline 1)
              position)]
    [row col]))

(s/defn row-col->position :- Int
  "Converts a row and column number to a position."
  [text :- Str
   row :- Int
   col :- Int]
  (let [all-lines (vec (split-lines text))
        lines (vec (take row all-lines))
        last-line (get all-lines row)
        lines (if (and last-line (>= (count last-line) col))
                (conj lines (subs last-line 0 col))
                lines)
        text (str/join \newline lines)]
    (count text)))

(s/defn create-edit-history []
  (atom {:current-state -1 :states []}))

(s/defn update-edit-history!
  "Updates the edit history atom."
  [edit-history :- Any
   state :- {Keyword Any}]
  (let [{:keys [current-state states]} @edit-history
        old-state (get states current-state)
        old-cursor-position (or (first (:original-cursor-position old-state)) 0)
        new-cursor-position (first (:original-cursor-position state))
        new-cursor-change (- new-cursor-position old-cursor-position)]
    ; if the last edit wasn't a single character after the previous edit, make it a separate undoable edit
    (when (or (<= (count states) 1)
              (not= new-cursor-change 1))
      (swap! edit-history update-in [:current-state] inc))
    (swap! edit-history update-in [:states] subvec 0 (:current-state @edit-history))
    (swap! edit-history update-in [:states] conj state)))

(s/defn update-cursor-position!
  "Updates only the cursor position."
  [edit-history :- Any
   cursor-position :- [Int]]
  (let [{:keys [current-state]} @edit-history]
    (when (>= current-state 0)
      (swap! edit-history assoc-in [:states current-state :cursor-position] cursor-position))))

(s/defn get-state :- {Keyword Any}
  "Returns the updated state of the text editor."
  ([text :- Str
    cursor-line :- Int
    cursor-x :- Int]
   (let [pos (row-col->position text cursor-line cursor-x)]
     (get-state text [pos pos])))
  ([text :- Str
    cursor-position :- [Int]]
   {:lines (split-lines text)
    :original-cursor-position cursor-position
    :cursor-position cursor-position}))

(s/defn undo! :- (maybe {Keyword Any})
  "Returns the last state from edit-history, or nil if there is none."
  [edit-history :- Any]
  (let [{:keys [current-state states]} @edit-history]
     (when-let [state (get states (dec current-state))]
       (swap! edit-history update-in [:current-state] dec)
       state)))

(s/defn redo! :- (maybe {Keyword Any})
  "Returns the next state from edit-history, or nil if there is none."
  [edit-history :- Any]
  (let [{:keys [current-state states]} @edit-history]
    (when-let [state (get states (inc current-state))]
      (swap! edit-history update-in [:current-state] inc)
      state)))

(s/defn can-undo? :- Bool
  "Returns true if we can undo."
  [edit-history :- Any]
  (if-let [{:keys [current-state states]} @edit-history]
    (some? (get states (dec current-state)))
    false))

(s/defn can-redo? :- Bool
  "Returns true if we can redo."
  [edit-history :- Any]
  (if-let [{:keys [current-state states]} @edit-history]
    (some? (get states (inc current-state)))
    false))
