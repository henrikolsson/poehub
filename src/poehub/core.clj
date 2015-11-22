(ns poehub.core
  (:import [java.io File])
  (:require [ring.middleware.content-type :refer [wrap-content-type]]
            [clojure.java.shell :as shell]
            [ring.middleware.defaults :refer :all]
            [ring.middleware.json :refer [wrap-json-response]]
            [clojure.data.json :as json]
            [ring.util.response :refer [response]]
            [clojure.tools.logging :as log]
            [compojure.core :refer [wrap-routes defroutes GET POST ANY]]
            [stasis.core :as stasis]
            [optimus.prime :as optimus]
            [optimus.assets :as assets]
            [optimus.optimizations :as optimizations]
            [optimus.strategies :refer [serve-live-assets]]
            [optimus.export]
            [optimus.link :as link]
            [hiccup.page :refer [html5]]
            [stasis.core :as stasis]
            [poehub.data :as data]
            [poehub.search :as search]))

(def target-dir "/tmp/poehub/")

(defn layout-page [request title page]
  (if (not-empty title)
    (let [uri (.replaceAll (:uri request) "/index.html" "")]
      (search/add title uri)))
  (html5
   [:head
    [:meta {:charset "utf-8"}]
    [:meta {:http-equiv "X-UA-Compatible" :content "IE=edge"}]
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
    [:title (if (not-empty title)
              (str "poehub - " title)
              "poehub")]
    [:link {:rel "stylesheet" :href "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css"
            :integrity "sha512-dTfge/zgoMYpP7QbHy4gWMEGsbsdZeCXz7irItjcC3sPUFtf0kuFbDz/ixG7ArTxmDjLXDmezHubeNikyKGVyQ=="
            :crossorigin="anonymous"}]
    [:link {:rel "stylesheet" :href (link/file-path request "/css/poehub.css")}]]
   [:body
    [:nav {:class "navbar navbar-inverse navbar-fixed-top"}
     [:div.container
      [:div.navbar-header
       [:button {:type "button"
                 :class "navbar-toggle collapsed"
                 :data-toggle "collapse"
                 :data-target "#navbar"
                 :aria-expanded "false"
                 :aria-controls "navbar"}
        [:span.sr-only "Toggle navigation"]
        [:span.icon-bar]
        [:span.icon-bar]
        [:span.icon-bar]]
       [:a.navbar-brand {:href "/"} "poehub"]]
      [:div {:id "navbar" :class "collapse navbar-collapse"}
       [:ul {:class "nav navbar-nav"}
        [:li [:a {:href "/skillgems/"} "Skillgems"]]
        [:li [:a {:href "/itemclasses/"} "Items"]]
        [:li [:a {:href "/affixes/"} "Affixes"]]
        [:li [:a {:href "/quests/"} "Quests"]]]]]]
    [:div.container
     [:div.main
      page]]
    [:script {:src "https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"} ""]
    [:script {:src "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/js/bootstrap.min.js"
              :integrity "sha512-K1qjQ+NcF2TYO/eI3M6v8EiNYZfA95pQumfvcVrTHtwQVDG+aHRqLi/ETn2uB+1JqwYqVG3LIvdm9lj6imS/pQ=="
              :crossorigin="anonymous"} ""]
    [:script {:src (link/file-path request "/js/poehub.js")} ""]]))

(defn index [ctx]
  (layout-page
   ctx
   nil
   [:div
    [:form {:method "POST" :onsubmit "return search();"}
     [:div.form-group
      [:input.form-control {:type "text" :name "query" :id "query" :placeholder ""}]]
     [:button {:type "submit" :class "btn btn-default"} "Search"]]
    [:br]
    [:div#search-results ""]]))
   
(defn get-skillgem-items []
  (filter #(or (= (get %1 "ItemClass") 19)
               (= (get %1 "ItemClass") 20)) data/base-item-types))

(defn skillgem-index [ctx]
  (let [filtered (filter #(or (= (get %1 "ItemClass") 19)
                              (= (get %1 "ItemClass") 20)) data/base-item-types)]
    (layout-page
     ctx
     nil
     [:div
      [:h1 "Skillgems"]
      (map #(vec
             [:p
              [:a {:href (str "/skillgems/" (get %1 "Row"))}  (get %1 "Name")]])
           filtered)])))

(defn find-all [data key val]
  (filter #(= (str (get %1 key)) (str val)) data))

(defn find-first [data key val]
  (first (find-all data key val)))

(defn skillgem-page [id]
  (fn [ctx]
    (let [item (find-first data/base-item-types "Row" id)
          meta (find-first data/active-skills "DisplayedName" (get item "Name"))]
    (layout-page
     ctx
     (get item "Name")
     [:div
      [:h1 (get item "Name")]
      (if meta
        [:div
         [:p (get meta "Description")]
         [:img {:src (get meta "WebsiteImage")}]])
      [:h3 "Quest rewards"]
      [:table
       [:tr
        [:th "Quest"]
        [:th "Difficulty"]
        [:th "Character"]]
       (map #(let [qk (get %1 "QuestKey")]
               [:tr
                [:td [:a {:href (str "/quests/" qk "/")}
                      (get (find-first data/quests "Row" qk) "Title")]]
                [:td (condp = (get %1 "Difficulty")
                       1 "Normal"
                       2 "Cruel"
                       3 "Merciless")]
                [:td (get (find-first
                           data/characters
                           "Row"
                           (get %1 "CharactersKey"))
                          "Name")]])
            (find-all data/quest-rewards "BaseItemTypesKey" id))]
      [:h3 "Experience levels"]
      [:table
       [:tr
        [:th "Level"]
        [:th "Experience"]]
       (map #(vector
              :tr
              [:td (get %1 "ItemCurrentLevel")]
              [:td (get %1 "Experience")])
            (find-all data/item-experience-per-level "BaseItemTypesKey" id))]
      (if meta
        (let [ge (filter
                  #(> (count (get %1 "Quality_StatsKeys")) 0)
                  (find-all data/granted-effects-per-level "ActiveSkillsKey" (get meta "Row")))
              stat-keys (if (> (count ge) 0)
                          (get (first ge) "StatsKeys")
                          '())]
          [:div
           [:h3 "Effects"]
           [:table
            [:tr
             [:th "level"]
             (map
              (fn [stat-key]
                [:th (get (find-first data/stats "Row" stat-key) "Id")])
              stat-keys)]
            (map
             (fn [effect]
               [:tr
                [:td (get effect "Level")]
                (for [i (range (count stat-keys))]
                  [:td (get effect (str "Stat" (+ i 1) "Value"))])])
             ge)]]))]))))

(defn get-skillgems []
  (merge {"/skillgems/" skillgem-index}
         (into
          {}
          (map #(vector (str "/skillgems/" (get %1 "Row") "/")
                        (skillgem-page (get %1 "Row")))
               (get-skillgem-items)))))

(defn quest-index [ctx]
  (layout-page
   ctx
   nil
   [:div
    [:h1 "Quests"]
    (map #(vector :p [:a {:href (str "/quests/" (get %1 "Row") "/")} (get %1 "Title")]) data/quests)]))

(defn quest-page [id]
  (fn [ctx]
    (let [quest (find-first data/quests "Row" id)
          title (get quest "Title")
          states (reverse (find-all data/quest-states "QuestKey" id))]
      (layout-page
       ctx
       title
       [:div
        [:h1 title]
        [:table
         [:tr
          [:th "Message"]
          [:th "Text"]]
         (map
          (fn [state]
            [:tr
             [:td (get state "Message")]
             [:td (get state "Text")]])
          states)]]))))

(defn get-quests []
  (merge {"/quests/" quest-index}
         (into
          {}
          (map #(vector (str "/quests/" (get %1 "Row") "/")
                        (quest-page (get %1 "Row")))
               data/quests))))

(defn item-classes-page [ctx]
  (layout-page
   ctx
   nil
   [:div
    [:h1 "Item classes"]
    (map
     #(vector
       :div
       [:a {:href (str "/itemclasses/" (get %1 "Id") "/")} (get %1 "Name")]
       [:br])
     (sort-by
      #(get %1 "Name")
      (filter
       #(> (.length (get %1 "Name")) 0)
       data/item-classes)))]))

(defn item-class-page [id name]
  (fn [ctx]
    (let [items (find-all data/base-item-types "ItemClass" id)]
      (layout-page
       ctx
       name
       [:div
        [:h1 name]
        (map #(vector :p (get %1 "Name")) items)]))))
  
(defn get-item-classes []
  (merge {"/itemclasses/" item-classes-page}
         (into
          {}
          (map #(vector (str "/itemclasses/" (get %1 "Id") "/")
                        (item-class-page (get %1 "Id") (get %1 "Name")))
               data/item-classes))))

(defn affixes-page [ctx]
  (layout-page
   ctx
   nil
   [:div
    [:h1 "Affixes"]
    (map
     #(vector :div
              [:a {:href (str "/affixes/" (get %1 "Row") "/")} (get %1 "Id")]
              [:br])
     (sort-by #(get %1 "Id") data/tags))]))

(defn affix-table [affixes]
  [:table
   [:tr
    [:th "Name"]
    [:th "Level"]
    [:th "Stat1"]
    [:th "Stat2"]
    [:th "Stat3"]
    [:th "Stat4"]]
   (map
    (fn [affix]
      [:tr
       [:td (get affix "Name")]
       [:td (get affix "Level")]
       (for [i (range 1 5)]
         (let [stat-min (get affix (str "Stat" i "Min"))
               stat-max (get affix (str "Stat" i "Max"))
               stat-text (get (find-first data/stats "Row" (get affix (str "StatsKey" i))) "Text")]
           [:td (str stat-min " - " stat-max " (" stat-text ")")]))])
    affixes)])

(defn find-index [coll v]
  (first
   (first
    (filter #(= (second %1) v)
            (map-indexed #(vector %1 %2) coll)))))

(defn find-affixes [tag-key type]
  (filter
   (fn [mod]
    (let [idx (find-index (get mod "SpawnWeight_TagsKeys") tag-key)]
      (and (not (= idx nil))
           (= (get mod "GenerationType") type)
           (> (nth (get mod "SpawnWeight_Values") idx) 0))))
   data/mods))


(defn affix-page [name id]
  (fn [ctx]
    (let [prefixes (find-affixes id 1)
          suffixes (find-affixes id 2)]
      (layout-page
       ctx
       nil
       [:div
        [:h1 name]
        [:h2 "Prefixes"]
        (affix-table prefixes)
        [:h2 "Suffixes"]
        (affix-table suffixes)]))))

(defn get-affixes []
  (merge {"/affixes/" affixes-page}
         (into
          {}
          (map
           #(vector (str "/affixes/" (get %1 "Row") "/")
                    (affix-page (get %1 "Id") (get %1 "Row")))
           data/tags))))

(defn get-pages []
  (merge {"/index.html" index}
         (get-affixes)
         (get-item-classes)
         (get-skillgems)
         (get-quests)))

(defn get-assets []
  (assets/load-assets "public" [#"/css/.*"
                                #"/js/.*"]))

(defn do-search [query]
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body (json/write-str (search/search query))})

(defroutes routes
  (POST "/search" [query] (do-search query))
  (stasis/serve-pages get-pages))

(def app (-> (wrap-defaults routes api-defaults)
             (optimus/wrap get-assets optimizations/none serve-live-assets)
             wrap-content-type))

(defn export []
  (let [assets (optimizations/all (get-assets) {})
        pages (get-pages)
        f (File. target-dir)
        site-dir (str target-dir "/site")]
    (if (not (.exists f))
      (.mkdirs f))
    (search/recreate)
    (stasis/empty-directory! target-dir)
    (optimus.export/save-assets assets site-dir)
    (stasis/export-pages pages site-dir {:optimus-assets assets})
    (log/info
     (shell/sh "elasticdump"
          (str "--input=http://127.0.0.1:9200/" search/index-name)
          (str "--output=" target-dir "/es_dump")))))

(comment
  (export))
