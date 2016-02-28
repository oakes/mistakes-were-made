(ns mistakes-were-made.core
  (:require [clojure.string :as str]
            [schema.core :refer [maybe either Any Str Int Keyword Bool]
             #?@(:clj [:as s])])
  #?(:cljs (:require-macros [schema.core :as s])))

(s/defn position->row-col :- [Int]
  "Converts an position to a row and column number."
  [text :- Str
   position :- Int]
  (let [s (subs text 0 position)
        last-newline (.lastIndexOf s \newline)
        col (- position last-newline)
        row (count (re-seq #"\n" s))]
    [row (dec col)]))

(s/defn row-col->position :- Int
  "Converts a row and column number to an position."
  [text :- Str
   row :- Int
   col :- Int]
  (let [s (str/join \newline (take row (str/split text #"\n")))
        position (+ (count s) (inc col))]
    position))

(s/defn create-edit-history []
  (atom {:current-state -1 :states []}))

(s/defn update-edit-history!
  "Updates the edit history atom."
  [edit-history :- Any
   state :- {Keyword Any}]
  (let [{:keys [current-state states]} @edit-history
        old-state (get states current-state)
        old-cursor-position (or (:original-cursor-position old-state) 0)
        new-cursor-position (:original-cursor-position state)
        new-cursor-change (- new-cursor-position old-cursor-position)]
    ; if the last edit wasn't a single character after the previous edit, make it a separate undoable edit
    (when (or (nil? old-state)
              (not= new-cursor-change 1))
      (swap! edit-history update-in [:current-state] inc))
    (swap! edit-history update-in [:states] subvec 0 (:current-state @edit-history))
    (swap! edit-history update-in [:states] conj state)))

(s/defn update-cursor-position!
  "Updates only the cursor position."
  [edit-history :- Any
   cursor-position :- Int]
  (let [{:keys [current-state]} @edit-history]
    (when (>= current-state 0)
      (swap! edit-history assoc-in [:states current-state :cursor-position] cursor-position))))

(s/defn custom-split-lines :- [Str]
  "Splits the string into lines."
  [s :- Str]
  (let [s (if-not (= \newline (last s))
            (str s "\n ")
            (str s " "))
        lines (str/split-lines s)
        last-line (last lines)
        last-line-len (max 0 (dec (count last-line)))]
    (conj (vec (butlast lines))
          (subs last-line 0 last-line-len))))

(s/defn get-state :- {Keyword Any}
  "Returns the updated state of the text editor."
  ([text :- Str
    cursor-line :- Int
    cursor-x :- Int]
   (get-state text (row-col->position text cursor-line cursor-x)))
  ([text :- Str
    cursor-position :- Int]
   {:lines (custom-split-lines text)
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

(s/defn init-debug! [])
