(ns aplo.spider.v8dy
  (:use hickory.core)
  (:require [clj-http.client :as http]
            [clojure.xml :as xml]
            [clojure.zip :as zip]
            [aplo.db :refer [save-film]]
            [digest :refer [md5]]))
(def url "http://v8dy.net/info")
(def base-url "http://v8dy.net")
(defn- trim [s] (apply str (next(pop (vec s)))))
(defn- parse-addr [s] {:addr (first (re-seq #"ftp.*[mp4|rmvb|mkv|avi]" s))})
(defn- parse-name-and-pic [s]
  (let [[pic name &other] (re-seq #"\x22[^=]+\x22" (first (re-seq #"<img.*>" s)))]
    {:pic (str base-url (trim pic)) :name (trim name)}))
(defn- request [id]
  (try
    (let [{:keys [status body]} (http/get (str url "/" id))]
      {:status 200 :data (conj (parse-addr body) (parse-name-and-pic body))})
    (catch Exception e {:status 500})))
(defn valid-region [scan-region]
  (->>(pmap
        (fn [id]
          (let [{:keys [status]} (request id)]
            (case status
              200 id
              nil)))
        scan-region)
    (filter identity)))
(def result (atom []))
(defn scan [region]
  (swap! result empty)
  (future
    (doseq [id region]
      (let [{:keys [status data]} (request id)]
        (if (= 200 status)
          (swap! result conj {:id id :data data})))))
  @result)
(defn save []
  (println "save..")
  (future
    (loop [{:keys [name addr pic] :as film} (:data (peek @result))]
      (if film
        (let [sign (.toUpperCase (md5 (str name addr pic)))]
          (save-film (conj film {:sign sign}))
          (swap! result pop)
          (recur (:data (peek @result))))))))