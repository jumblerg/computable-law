(set-env!
  :asset-paths  #{"rsc"}
  :source-paths #{"src"}
  :dependencies '[[org.clojure/clojure       "1.8.0"     :scope "provided"]
                  [org.clojure/tools.nrepl   "0.2.12"    :scope "test"]
                  [adzerk/boot-cljs          "1.7.228-1" :scope "test"]
                  [adzerk/boot-cljs-repl     "0.3.0"     :scope "test"]
                  [adzerk/boot-reload        "0.4.7"     :scope "test"]
                  [adzerk/bootlaces          "0.1.13"    :scope "test"]
                  [com.cemerick/piggieback   "0.2.1"     :scope "test"]
                  [hoplon/boot-hoplon        "0.1.13"    :scope "test"]
                  [tailrecursion/boot-jetty  "0.1.3"     :scope "test"]
                  [weasel                    "0.7.0"     :scope "test"]
                  [org.clojure/clojurescript "1.8.51"]
                  [hoplon/ui                 "0.0.1-SNAPSHOT"]
                  [markdown-clj              "0.9.89"]])

(require
  '[adzerk.bootlaces         :refer :all]
  '[adzerk.boot-cljs         :refer [cljs]]
  '[adzerk.boot-cljs-repl    :refer [cljs-repl start-repl]]
  '[adzerk.boot-reload       :refer [reload]]
  '[hoplon.boot-hoplon       :refer [hoplon]]
  '[tailrecursion.boot-jetty :refer [serve]])

(def +version+ "0.0.1-SNAPSHOT")

(bootlaces! +version+)

(deftask develop
  [p port PORT int "The port from which to serve the application."]
  (comp (watch) (speak) (hoplon) (cljs-repl) (reload) (cljs) (serve :port (or port 4000))))

(deftask build []
  (comp (speak) (hoplon) (cljs :optimizations :advanced) (target :dir ["tgt"])))
