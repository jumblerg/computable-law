(ns+ human-dynamics.computeable-law
  (:page
    "index.html")
  (:refer-clojure
    :exclude [- name next])
  (:require
    [javelin.core    :refer [cell= defc defc=]]
    [hoplon.core     :refer [for-tpl when-tpl case-tpl defelem]]
    [hoplon.ui       :refer [elem button image toggle form window *scroll*]]
    [hoplon.ui.attrs :refer [- c r s b d]]))

;;; constants ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def org-short "LTL")
(def org-long "Legal Technology Laboratory")

(def content-url  "http://data.computablelaw.org/content.edn")
(def resource-url "http://data.computablelaw.org/resources/")
(def icon-url     "https://fonts.googleapis.com/icon?family=Material+Icons")
(def font-url     "https://fonts.googleapis.com/css?family=RobotoDraft:400,500,700,400italic")

;;; utils ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn prev [coll i] (prn :coll coll :i i) (mod (inc i) (count coll)))
(defn next [coll i] (prn :coll coll :i i) (mod (inc i) (count coll)))

(defn ->time [str]
  (.toLocaleTimeString (js/Date. str) (.-language js/navigator) #js{:svour "2-digit" :minute "2-digit"}))

;;; state ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defc db {})

;;; service ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(.get js/jQuery content-url #(swap! db merge (cljs.reader/read-string %)))

;;; query ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defc= route (-> db :route))
(defc= path  (-> route first))
(defc= root  (-> path first))
(defc= state (-> path last))
(defc= qargs (-> route second))
(defc= ident (-> qargs :id))

(defc= views (-> db :views))
(defc= view  (some #(when (= (:view %) root) %) views))
(defc= name  (-> view :name))
(defc= title (-> view :title))
(defc= body  (-> view :body))
(defc= items (-> view :items))
(defc= item  (get items ident))

;;; command ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn change-route [db route]
  (assoc db :route route))

(defn change-state [db path & qargs]
  (change-route db [path (not-empty (apply hash-map qargs))]))

(defn initiate [db route status _]
  (change-route db (if (empty? route) [[:projects]] route)))

;;; view styles ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

; breakpoints
(def sm 760)
(def md 1240)
(def lg 1480)

; sizes
(def pad-sm 8)
(def pad-md 16)
(def pad-lg 24)

;; colors

(def black      (c 0x000000))
(def grey       (c 0x9E9E9E))
(def white      (c 0xFFFFFF))
(def grey-300   (c 0xE0E0E0))
(def font-black (c 0x333333))

; fonts
(def helvetica ["Helvetica Neue" "Lucida Grande" :sans-serif])

; text
(defelem html [attrs html]
  (set! (.-innerHTML (elem attrs)) html))

(defelem view-title [attrs elems]
  (elem :sh (r 1 1) :f 38 :ff helvetica :fx :uppercase :ft :500
    attrs elems))

(defelem view-subtitle [attrs elems]
  (elem :sh (r 1 1) :pl 4 :fi :italic attrs elems))

(defelem view-body [attrs elems]
  (html :sh (r 1 1) attrs elems))

(defelem section-title [attrs elems]
  (elem :sh (r 1 1) :f 23
    attrs elems))

(defelem section-subtitle [attrs elems]
  (elem :sh (r 1 1) :pl 4 :pb 12 :fi :italic
    attrs elems))

(defelem section-body [attrs elems]
  (html :sh (r 1 1)
     attrs elems))

;;; view components ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defelem icon [attrs elems]
  (elem :f 24 :fh 24 :ff "Material Icons" :m :pointer :fr :optimizeLegibility
    attrs elems))

(defelem footer-menu [attrs elems]
  (elem :sh (r 1 1) attrs
    (let [n (count elems)]
      (for-tpl [elem elems]
        (elem :sh (r 1 n))))))

(defelem card [{:keys [title url] :as attrs} elems]
  (elem :bc grey-300 (dissoc attrs :title :url) :d (d 0 1 (c 0 0 0 0.37) 4)
    (image :url url)
    (elem :sh (r 1 1) :p pad-md :g pad-sm
      (elem :sh (r 1 1) :f 16 :fh 15 :fc font-black :ft :500
        title)
      (elem :sh (r 1 1) :f 14 :fh 15 :fc font-black
        elems))))

(defelem feature-box [{:keys [title] :as attrs} elems]
  (elem :sh (b (r 1 3) sm 100) (dissoc attrs :title)
    (elem :sh (r 1 1) :ph 3 :av :middle
      title)
    (elem :sh (r 1 1) :sv (- (r 1 1) 26) :f 30
     elems)))

;;; view views ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn basic-view []
  (elem :sh (r 1 1) :p 42
    (view-title title)
    (view-body  body)))

(defelem event-view [attrs _]
  (elem :sh (r 1 1) :p 42
    (view-title    (cell= (:title item)))
    (view-subtitle (cell= (apply str (interpose " " (:speakers item)))))
    (view-body     (cell= (:description item)))))

(defelem events-view [_ _]
  (elem :sh (r 1 1) :p 42
    (for-tpl [[i {:keys [title speakers description start-time stop-time]}] (cell= (map-indexed vector items))]
      (elem :gh 64
        (elem :sh 76 :pv 4 :gv 16 :bl 2 :bc (c 0 0 0 0.25) :ah :right
          (elem (cell= (->time start-time)))
          (elem (cell= (->time stop-time))))
        (elem :sh (- (r 1 1) 150) :m :pointer :click #(swap! db change-state [:events :event] :id @i)
          (section-title
             title)
          (section-subtitle
             (cell= (apply str (interpose " " speakers))))
          (section-body
             (cell= (when description (subs description 0 100)))))))))

(defelem project-view [attrs _]
  (elem :sh (r 1 1) :p 42 :g 24
    (elem :sh (r 1 1)
      (view-title     (cell= (:title    item)))
      (view-subtitle  (cell= (str (:focus item) " • " (:status   item)))))
    (view-body (cell= (apply str (interpose "\n" (:partners  item)))))
    (view-body (cell= (:summary  item)))
    (view-body (cell= (:goals    item)))
    (view-body (cell= (:outcomes item)))
    (view-body (cell= (apply str (interpose "\n" (:resources item)))))))

(defn projects-view []
  (elem :sh (r 1 1) :p 42 :g 24
    (for-tpl [[i {:keys [title focus status]}] (cell= (map-indexed vector items))]
      (elem :sh (r 1 1) :m :pointer :click #(swap! db change-state [:projects :project] :id @i)
        (section-title    title)
        (section-subtitle focus)))))

(defn organizers-view []
  (elem :sh (r 1 1) :p 42
    (view-title title)))

(defelem photo-view [attrs _]
  (elem :sh (r 1 1) :sv (r 1 1) :p 42 :g 42 :ah :center :av :middle
    (icon :f 64 :click #(swap! db change-state [:photos :photo] :id (prev @items @ident)) "keyboard_arrow_left")
    (image :sh (r 4 5) :d (d 0 1 (c 0 0 0 0.37) 4) :url (cell= (when-let [url (:image item)] (.log js/console "url" url) (str resource-url url))) :click #(swap! db change-state [:photos]))
    (icon :f 64 :click #(swap! db change-state [:photos :photo] :id (next @items @ident)) "keyboard_arrow_right")))

(defn photos-view []
  (elem :sh (r 1 1) :p 42 :g 42 :scroll true
    (for-tpl [[i {:keys [thumb]}] (cell= (map-indexed vector items))]
      (image :sh (b (r 1 1) sm (r 1 2) md (r 1 3) lg (r 1 4)) :d (d 0 1 (c 0 0 0 0.37) 4) :m :pointer :click #(swap! db change-state [:photos :photo] :id @i) :url (cell= (when thumb (str resource-url thumb)))))))

(defn notes-view []
  (elem :sh (r 1 1) :p 42 :g 42
    (view-title title)))

;;; application ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defc menu-open false)

(defelem sidebar [attrs elems]
  (elem :pv 40 :gv 30 :ah :center :c black :click #(reset! menu-open false) :o 0.3 attrs
    (image :sv 130 :m :pointer :url "mit-ml-logo.jpg" :click #(do (reset! menu-open false) (swap! db change-state [:projects])))
    (elem :sh (r 1 1) :sv (- (r 1 1) (+ 130 54 30 30)) :gv 46 :bc grey-300
      (for-tpl [{:keys [title view]} views]
        (elem :sh (r 1 1) :ph 40 :pv 4 :av :middle :bl 3 :bcl (cell= (if (= view root) white black)) :f 18 :ff helvetica :ft :800 :fc white :m :pointer :click #(do (reset! menu-open false) (swap! db change-state [@view]))
          (elem :pt 2 :fx :lowercase
            title))))
    (elem :sh (r 1 1) :ph 36 :ff helvetica :fc white
      (image :sh 130 :url "creative-commons-logo.png" :m :pointer :click #(.open js/window "https://creativecommons.org/")))))

(defelem main [attrs elems]
  (elem attrs
    (elem :sh (r 1 1) :sv 64 :ph pad-lg :pv pad-sm :gh pad-lg :av :middle :bb 1 :bc (c 0 0 0 0.15) ;:d (d 0 1 (c 0 0 0 0.37) 4)
      (when-tpl (b true lg false)
        (icon :click #(swap! menu-open not) :fc black "menu"))
      (elem :sh (b (- (r 1 1) (+ 24 130 pad-lg pad-lg)) lg (- (r 1 1) (+ 130 24))) :f 22 :ff helvetica :fx :uppercase :ft :500
        (b (cell= (str org-short " " title)) sm (cell= (str org-long " " title))))
      (elem :g pad-sm :ah :right :av :middle
        (image :sh 60 :url "kauffman-logo.png" :m :pointer :click #(.open js/window "http://www.kauffman.org/"))))
    (elem :sh (r 1 1) :sv (- (r 1 1) 64) :scroll true
      elems)))

(defelem overlay [attrs elems]
  (elem :c (c 0 0 0 0.5) :click #(reset! menu-open false)
    attrs elems))

(window
  :title        (cell= (str org-short " - " name))
  :route        route
  :styles       [icon-url font-url]
  :initiated    (partial swap! db initiate)
  :routechanged (partial swap! db change-route)
  :ff helvetica :fr :optimizeLegibility :fm :antialiased
  (when-tpl (b menu-open lg true)
    (sidebar :sh 240 :sv (r 1 1) :xl (b menu-open lg false)))
  (main :sh (b (r 1 1) lg (- (r 1 1) 240)) :sv (r 1 1)
    (case-tpl state
      :basic      (basic-view)
      :event      (event-view)
      :events     (events-view)
      :project    (project-view)
      :projects   (projects-view)
      :organizers (organizers-view)
      :photo      (photo-view)
      :photos     (photos-view)
      :notes      (notes-view)))
  (overlay :xl 240 :xr 0 :sh (r 1 1) :sv (r 1 1) :v (b menu-open lg false)))
