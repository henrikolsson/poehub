(ns poehub.search
  (:require [clojure.tools.logging :as log]
            [clojurewerkz.elastisch.rest :as esr]
            [clojurewerkz.elastisch.rest.admin :as admin]
            [clojurewerkz.elastisch.rest.index :as esi]
            [clojurewerkz.elastisch.rest.document :as esd]
            [clojurewerkz.elastisch.query :as q]
            [clojurewerkz.elastisch.rest.response :as esrsp]
            [clojure.pprint :as pp]))

(def index-name "poehub_testing")

(def mapping-types {"page" {:properties
                            {:title {:type "string" :store "yes"}
                             :url {:type "string" :store "yes"}}}})

(def conn (esr/connect "http://127.0.0.1:9200"))

(defn recreate []
  (esi/delete conn index-name)
  (esi/create conn index-name :mappings mapping-types))

(defn add [title url]
  (log/info "adding:" title "to:" url)
  (esd/create conn index-name "page" {:title title :url url} :id url))

  
(defn search [term]
  (let [res (esd/search conn index-name "page" :query {:query_string {:query term}})]
    (esrsp/hits-from res)))

(comment (recreate)
         (add "Poison Arrow" "/skillgem/123")
         (search "arrow"))

;; (admin/register-snapshot-repository
;;  conn
;;  "test"
;;  {:type "fs"
;;   :settings {:compress "false"
;;              :location "/tmp/elasticsearch/repository/"}})

;; (admin/take-snapshot conn "test" "snap1")
