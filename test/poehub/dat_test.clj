(ns poehub.dat-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [poehub.dat :refer :all]))

(def test-file (.getPath (io/resource "Characters.dat")))

(def expected {"Unknown13" 0, "BaseMaxLife" 50, "ACTFile" "Metadata/Characters/Str/Str.act", "StartWeapon_BaseItemTypesKey" 590, "BaseMaxMana" 40, "Unknown6" 1, "AOFile" "Metadata/Characters/Str/Str.ao", "IntroSoundFile" "Audio/Dialogue/Character/Str/Intro/Ma_01_Intro.ogg", "Unknown15" 6, "Row" 0, "Icon" "", "Keys0" '(3927), "Description" "I am a warrior, raised to honour my Ancestors, to die with a weapon in my hand and the Karui Way in my blood. \r\n\r\nOriath chained me, made me its slave. For three years I have lived without my family, my pride, my Way. \r\n\r\nI welcome Exile. I welcome Wraeclast. Hear me, Ancestors! A slave stands behind you. A warrior stands before you. \r\n\r\nAnd Death walks at our side.", "MinDamage" 2, "CharacterSize" 2, "BaseDexterity" 14, "Unknown28" 77, "WeaponSpeed" 833, "StartSkillGem_BaseItemTypesKey" 1341, "BaseIntelligence" 14, "Unknown16" 1, "Name" "Marauder", "BaseStrength" 32, "MaxAttackDistance" 4, "MaxDamage" 8, "Id" "Metadata/Characters/Str/Str", "Unknown14" 0})

(deftest can-parse
  (let [parsed (first (parse test-file))]
    (log/info parsed)
    (is (= expected
           parsed))))

(deftest can-bytes-to-uint-le
  (is (= 352 (bytes-to-uint-le (into-array Byte/TYPE '(96 01 00 00))))))

