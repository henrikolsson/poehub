(ns poehub.pages
  (:import [java.util Date])
  (:require [hiccup.page :refer [html5]]
            [optimus.link :as link]
            [poehub.data :as data]
            [poehub.dat :as dat]
            [poehub.search :as search]))

(defn layout-page [request title page]
  (if (not-empty title)
    (let [uri (.replaceAll (:uri request) "/index.html" "/")]
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
        [:li [:a {:href "/quests/"} "Quests"]]
        [:li [:a {:href "/archive/"} "Archive"]]
        [:li [:a {:href "/data/"} "Data"]]]]]]
    [:div.container
     [:div.main
      page]]
    [:footer
     [:strong "Generated: "]
     [:span (Date.)]
     [:strong " Content Version: "]
     [:span (dat/get-data-version)]]
    [:script {:src "https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"} ""]
    [:script {:src "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/js/bootstrap.min.js"
              :integrity "sha512-K1qjQ+NcF2TYO/eI3M6v8EiNYZfA95pQumfvcVrTHtwQVDG+aHRqLi/ETn2uB+1JqwYqVG3LIvdm9lj6imS/pQ=="
              :crossorigin="anonymous"} ""]
    [:script {:src "//twitter.github.io/typeahead.js/releases/latest/typeahead.bundle.js"}]
    [:script {:src (link/file-path request "/js/poehub.js")} ""]
    [:script "
  (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
  (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
  m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
  })(window,document,'script','//www.google-analytics.com/analytics.js','ga');

  ga('create', 'UA-70467764-1', 'auto');
  ga('send', 'pageview');"]]))

(defn index [ctx]
  (layout-page
   ctx
   nil
   [:div.center
    [:h1 "poehub"]
    [:br]
    [:form {:method "POST" :onsubmit "return formSearch();"}
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

(defn find-all-contains [data key val]
  (filter #(.contains (get %1 key) val) data))

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
        [:h3 "Quest vendor rewards"]
        [:table
         [:tr
          [:th "Quest"]
          [:th "Difficulty"]
          [:th "Character"]]
         (map #(let [qk (get %1 "Row")]
                 [:tr
                  [:td [:a {:href (str "/quests/" qk "/")}
                        (get %1 "Title")]]
                  [:td (condp = (get %1 "Difficulty")
                         1 "Normal"
                         2 "Cruel"
                         3 "Merciless"
                         "N/A")]
                  [:td (get (find-first
                             data/characters
                             "Row"
                             (get %1 "CharactersKey"))
                            "Name")]])
              (map
               (fn [quest-vendor-reward]
                 (let [quest (find-first data/quests "Unknown11" (get quest-vendor-reward "QuestState"))
                       quest-state (first (find-all-contains data/quest-states "QuestStates" (get quest-vendor-reward "QuestState")))]
                   (if quest
                     (assoc quest "CharactersKey" (first (get quest-vendor-reward "CharactersKeys")))
                     (if quest-state
                       (assoc (find-first data/quests "Row" (get quest-state "QuestKey"))
                              "CharactersKey" (first (get quest-vendor-reward "CharactersKeys")))))))
               (find-all-contains data/quest-vendor-rewards "BaseItemTypesKeys" id)))]
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
               ge)]]))
        [:h3 "Experience levels"]
        [:table
         [:tr
          [:th "Level"]
          [:th "Experience"]]
         (map #(vector
                :tr
                [:td (get %1 "ItemCurrentLevel")]
                [:td (get %1 "Experience")])
              (find-all data/item-experience-per-level "BaseItemTypesKey" id))]]))))

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
    (map
     #(vector :p [:a {:href (str "/quests/" (get %1 "Row") "/")} (get %1 "Title")])
     (sort-by
      #(get %1 "UniqueId")
      (filter
       #(re-matches #"^a[0-9]+q[0-9]+$" (get %1 "UniqueId"))
       data/quests)))]))

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
               (sort-by
                #(get %1 "UniqueId")
                (filter
                 #(re-matches #"^a[0-9]+q[0-9]+$" (get %1 "UniqueId"))
                 data/quests))))))

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
(defn item-affix-tags []
  (let [tag-keys (distinct
                  (flatten
                   (let [relevant-mods (filter #(or (= (get %1 "GenerationType") 1)
                                                    (= (get %1 "GenerationType") 2))
                                               data/mods)]
                     (map
                      (fn [mod]
                        (let [values (get mod "SpawnWeight_Values")
                              tags (get mod "SpawnWeight_TagsKeys")]
                          (for [i (range (count values))]
                            (if (> (nth values i 0) 0)
                              (nth tags i)
                              nil))))
                      relevant-mods))))]
    (filter #(.contains tag-keys (get %1 "Row")) data/tags)))

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
     (sort-by #(get %1 "Id") (item-affix-tags)))]))


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
           (item-affix-tags)))))

(defn server-error [ctx]
  (layout-page
   ctx
   nil
   [:div
    [:h1 "Server Error!"]]))

(defn not-found [ctx]
  (layout-page
   ctx
   nil
   [:div
    [:h1 "Page not found!"]]))

(defn get-pages []
  (merge {"/index.html" index
          "/404.html" not-found
          "/50x.html" server-error}
         (get-affixes)
         (get-item-classes)
         (get-skillgems)
         (get-quests)))
