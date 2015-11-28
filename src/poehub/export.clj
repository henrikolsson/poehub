(ns poehub.export
  (:import [java.io File])
  (:require [optimus.optimizations :as optimizations]
            [optimus.assets :as assets]
            [stasis.core :as stasis]
            [optimus.export]
            [poehub.search :as search]
            [poehub.dat :as dat]
            [clojure.tools.logging :as log]
            [clojure.java.shell :as shell]
            [poehub.config :as config]
            [poehub.ggpk :as ggpk]
            [poehub.pages :refer [get-pages]]))

(defn get-assets []
  (assets/load-assets "public" [#"/css/.*"
                                #"/js/.*"]))

(defn export []
  (log/info "exporting...")
  (let [version (let [v (ggpk/extract-data-files config/content-file config/data-dir)]
                  (dat/set-data-version! v)
                  (dat/jsonify-all)
                  (log/info "version:" v)
                  v)
        assets (optimizations/all (get-assets) {})
        pages (do
                (log/info "getting pages...")
                (get-pages))
        site-dir (File. (str config/target-dir File/separator "site" File/separator version))]
    (log/info "site dir: " site-dir)
    (if (not (.exists site-dir))
      (.mkdirs site-dir))
    (search/recreate)
    (stasis/empty-directory! (.getCanonicalPath site-dir))
    (log/info "saving assets...")
    (optimus.export/save-assets assets (.getCanonicalPath site-dir))
    (log/info "exporting pages...")
    (stasis/export-pages pages site-dir {:optimus-assets assets})
    (log/info "dumping elasticsearch index...")
    (shell/sh "elasticdump"
              (str "--input=http://127.0.0.1:9200/" search/index-name)
              (str "--output=" config/target-dir "/es_dump"))))
