(ns aplo.spider.ys66
  (:require [clj-http.client :as http]
            [aplo.db :refer [save-film]]
            [digest :refer [md5]]))

(def base-url "http://www.66ys.cc/")
(def url-1080p "http://www.66ys.cc/1080p/")
(defn- trim [s] (apply str (next(pop (vec s)))))
(def match-1080p-pages
  #"<td><a href=\S+ target=\S+><img src=\S+ alt=\S+ width=\x22\d+\x22 height=\x22\d+\x22 border=\x22\d+\x22 /></a></td>")
(def match-1080p-page
  #"\x22[^=]+\x22")
(def match-1080p-total-film
  #"<a title=\x22Total record\x22>&nbsp;<b>\d+</b>")
(def match-1080p-download-addr
  #"http://\S+.torrent")

(defn total-1080p-page [url]
  (let [total-film (#(first (re-seq #"\d+" %)) (first (re-seq match-1080p-total-film (slurp url))))
        page-num 10 pages (+ (int(/ (Integer. total-film) page-num)) (mod (Integer. total-film) page-num))]
      (cons (str url "index.htm") (map #(str url "index_" % ".htm") (range 2 (inc pages))))))
(defn- film-download-addr [url]
  (first (re-seq match-1080p-download-addr (slurp url))))
(defn- film-info [body]
  (->>(re-seq match-1080p-pages body)
    (map #(re-seq match-1080p-page %))
    (map (fn [[addr _ pic name &ls]] {:addr (film-download-addr(trim addr)) :pic (trim pic) :name (trim name)}))))
  ; (pmap
  ;     (fn [[addr _ pic name &ls]]
  ;       {:addr addr :pic pic :name name})
  ;     (pmap #(pmap trim (re-seq match-1080p-page %)) (re-seq match-1080p-pages body))))

(def result (atom []))
(defn- request [url]
  (try
    (let [body (slurp url)]
      (println url)
      {:status 200 :data (film-info body)})
    (catch Exception e {:status 500})))
(defn scan [region]
  (swap! result empty)
  (future
    (doseq [url region]
      (let [{:keys [status data]} (request url)]
        (if (= 200 status)
          (swap! result conj {:id url :data data})))))
  @result)
(defn save []
  (future
    (loop [{:keys [name addr pic] :as film} (:data (peek @result))]
      (if film
        (let [sign (.toUpperCase (md5 (str name addr pic)))]
          (save-film (conj film {:sign sign}))
          (swap! result pop)
          (recur (:data (peek @result))))))))