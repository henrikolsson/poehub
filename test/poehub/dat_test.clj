(ns poehub.dat-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [poehub.dat :refer :all]))

(def test-file (.getPath (io/resource "Characters.dat")))

(def expected {"Unknown28" 77 "BaseStrength" 32 "BaseIntelligence" 14 "Unknown17" 2 "Unknown18" "Audio/Dialogue/Character/Str/Intro/Ma_01_Intro.ogg" "Unknown15" 6 "Row" 0 "Unknown27" 0 "Actor" "Metadata/Characters/Str/Str.act" "BaseMaxLife" 50 "BaseDexterity" 14 "Unknown16" 1 "Data0" '(3927) "Unknown14" 0 "Description" "I am a warrior, raised to honour my Ancestors, to die with a weapon in my hand and the Karui Way in my blood. \r\n\r\nOriath chained me, made me its slave. For three years I have lived without my family, my pride, my Way. \r\n\r\nI welcome Exile. I welcome Wraeclast. Hear me, Ancestors! A slave stands behind you. A warrior stands before you. \r\n\r\nAnd Death walks at our side." "Unknown11" 1341 "AnimatedObject" "Metadata/Characters/Str/Str.ao" "Icon" "" "BaseMaxMana" 40 "Unknown12" 0 "Name" "Marauder" "MinDamage" 2 "WeaponSpeed" 833 "MaxDamage" 8 "MaxAttackDistance" 4 "Id" "Metadata/Characters/Str/Str" "Unknown13" 0 "Unknown26" 590 "Unknown6" 1})

(deftest can-parse
  (let [parsed (first (parse test-file))]
    (log/info parsed)
    (is (= expected
           parsed))))

(deftest can-bytes-to-uint-le
  (is (= 352 (bytes-to-uint-le (into-array Byte/TYPE '(96 01 00 00))))))
