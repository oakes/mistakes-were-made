(ns mistakes-were-made.core
  (:require [clojure.data :refer [diff]]
            [clojure.string :as str]
            [schema.core :refer [maybe either Any Str Int Keyword Bool]
             #?@(:clj [:as s])])
  #?(:cljs (:require-macros [schema.core :as s])))

(s/defn index->row-col :- [Int]
  "Converts an index to a row and column number."
  [text :- Str
   index :- Int]
  (let [s (subs text 0 index)
        last-newline (.lastIndexOf s \newline)
        col (- index last-newline)
        row (count (re-seq #"\n" s))]
    [row (dec col)]))

(s/defn row-col->index :- Int
  "Converts a row and column number to an index."
  [text :- Str
   row :- Int
   col :- Int]
  (let [s (str/join \newline (take row (str/split text #"\n")))
        index (+ (count s) (inc col))]
    index))

(s/defn count-lines-changed
  "Returns the number of lines changed between the new lines and old lines."
  [new-lines :- [Str]
   old-lines :- [Str]]
  (->> (diff new-lines old-lines) first (remove nil?) count))

(s/defn create-edit-history []
  (atom {:current-state -1 :states []}))

(s/defn update-edit-history!
  "Updates the edit history atom."
  [edit-history :- Any
   state :- {Keyword Any}]
  (let [{:keys [current-state states]} @edit-history
        old-state (get states current-state)
        old-lines (:lines old-state)
        new-lines (:lines state)
        old-lines-changed (:lines-changed old-state)
        new-lines-changed (count-lines-changed new-lines old-lines)
        state (assoc state :lines-changed new-lines-changed)]
    ; if the last two edits only affected one line, replace the last edit instead of adding a new edit
    (when (not= old-lines-changed new-lines-changed 1)
      (swap! edit-history update-in [:current-state] inc))
    (swap! edit-history update-in [:states] subvec 0 (:current-state @edit-history))
    (swap! edit-history update-in [:states] conj state)))

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
   (get-state text (row-col->index text cursor-line cursor-x)))
  ([text :- Str
    cursor-index :- Int]
   {:lines (custom-split-lines text)
    :index cursor-index}))

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
