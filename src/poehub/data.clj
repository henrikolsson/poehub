(ns poehub.data
  (:import [java.io File])
  (:require [clojure.tools.logging :as log]
            [poehub.dat :as dat]))

(def characters
  (dat/parse-memoized "Characters.dat"))
(def base-item-types
  (dat/parse-memoized "BaseItemTypes.dat"))
(def quests
  (dat/parse-memoized "Quest.dat"))
(def quest-states
  (dat/parse-memoized "QuestStates.dat"))
(def quest-rewards
  (dat/parse-memoized "QuestRewards.dat"))
(def quest-vendor-rewards
  (dat/parse-memoized "QuestVendorRewards.dat"))
(def active-skills
  (dat/parse-memoized "ActiveSkills.dat"))
(def item-classes
  (dat/parse-memoized "ItemClassesDisplay.dat"))
(def item-experience-per-level
  (dat/parse-memoized "ItemExperiencePerLevel.dat"))
(def granted-effects-per-level
  (dat/parse-memoized "GrantedEffectsPerLevel.dat"))
(def stats
  (dat/parse-memoized "Stats.dat"))
(def tags
  (dat/parse-memoized "Tags.dat"))
(def mods
  (dat/parse-memoized "Mods.dat"))
(def gem-tags
  (dat/parse-memoized "GemTags.dat"))
(def skill-gems
  (dat/parse-memoized "SkillGems.dat"))
(def granted-effects
  (dat/parse-memoized "GrantedEffects.dat"))
