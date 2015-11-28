(defproject poehub "0.0.1-SNAPSHOT"
  :description "poehub"
  :url "http://poehub.org"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [compojure "1.4.0"]
                 [stasis "2.2.2"]
                 [ring "1.4.0"]
                 [ring/ring-json "0.4.0"]
                 [ring/ring-defaults "0.1.5"]
                 [org.clojure/data.json "0.2.6"]
                 [hiccup "1.0.5"]
                 [ch.qos.logback/logback-classic "1.1.3"]
                 [clojurewerkz/elastisch "2.1.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [com.google.guava/guava "18.0"]
                 [commons-codec/commons-codec "1.10"]
                 [optimus "0.18.3"]]
  :plugins [[lein-ring "0.9.7"]]
  :profiles {:dev
             {:jvm-opts ["-Xmx1g"
                         "-Dcom.sun.management.jmxremote"
                         "-Dcom.sun.management.jmxremote.ssl=false"
                         "-Dcom.sun.management.jmxremote.authenticate=false"
                         "-Dcom.sun.management.jmxremote.port=43210"]}}
  :aliases {"build-site" ["run" "-m" "poehub.export/export"]}
  :ring {:handler poehub.core/app
         :nrepl {:start? true}})

