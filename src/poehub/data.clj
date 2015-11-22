(ns poehub.data
  (:require [poehub.dat :as dat]))

(def data-path (str "/home/henrik/dev/poehub/Data/"))

(def characters
  (dat/parse-memoized (str data-path "Characters.dat")))
(def base-item-types
  (dat/parse-memoized (str data-path "BaseItemTypes.dat")))
(def quests
  (dat/parse-memoized (str data-path "Quest.dat")))
(def quest-states
  (dat/parse-memoized (str data-path "QuestStates.dat")))
(def quest-rewards
  (dat/parse-memoized (str data-path "QuestRewards.dat")))
(def active-skills
  (dat/parse-memoized (str data-path "ActiveSkills.dat")))
(def item-classes
  (dat/parse-memoized (str data-path "ItemClassesDisplay.dat")))
(def item-experience-per-level
  (dat/parse-memoized (str data-path "ItemExperiencePerLevel.dat")))
(def granted-effects-per-level
  (dat/parse-memoized (str data-path "GrantedEffectsPerLevel.dat")))
(def stats
  (dat/parse-memoized (str data-path "Stats.dat")))
(def tags
  (dat/parse-memoized (str data-path "Tags.dat")))
(def mods
  (dat/parse-memoized (str data-path "Mods.dat")))
