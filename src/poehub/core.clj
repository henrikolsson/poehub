(ns poehub.core
  (:require [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.defaults :refer :all]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [compojure.core :refer [wrap-routes defroutes GET POST ANY]]
            [stasis.core :as stasis]
            [optimus.prime :as optimus]
            [optimus.assets :as assets]
            [optimus.optimizations :as optimizations]
            [optimus.strategies :refer [serve-live-assets]]
            [poehub.search :as search]
            [poehub.pages :as pages]))

(defn get-assets []
  (assets/load-assets "public" [#"/css/.*"
                                #"/js/.*"]))

(defn do-search [query]
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body (json/write-str (search/search query))})

(defroutes routes
  (POST "/search" [query] (do-search query))
  (stasis/serve-pages pages/get-pages))

(def app (-> (wrap-defaults routes api-defaults)
             (optimus/wrap get-assets optimizations/none serve-live-assets)
             wrap-content-type))

