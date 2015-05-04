(ns cljs.lst
  (:use [jayq.core :only [$ css html]])
  (:require [ajax.core :refer [GET POST]]
            [cljs.chart]
            [cljs.test]))
(enable-console-print!)
(html ($ :h1) "Power by Clojure Script!")
(defn handler [data]
  (println (str data))
  (html ($ :#main) (str (:info data))))
(def url "http://app.itrich.cn/loop")
(POST url {:handler handler :response-format :json :keywords? true})
(defn ^:export hello [name]
  (js/hello name))
(defn ^:export showChart [id]
  (js/Chart. (.getContext (js/document.getElementById id) "2d")))