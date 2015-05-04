(ns aplo.util
  (:require [selmer.parser :refer [render-file set-resource-path! cache-on! cache-off!]]
            [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.data.codec.base64 :as b64]))
(defn render
  ([file-url]
    (render-file (str "templates" file-url) nil))
  ([file-url data]
    (render-file (str "templates" file-url) data)))
(defn logger [^clojure.lang.PersistentArrayMap form]
    (spit
        (str "/home/rich/logs/lrc." (.format (java.text.SimpleDateFormat. "yyyy-MM-dd") (java.util.Date.)) ".log")
        (str (json/write-str form) "\n") :append true))
(defn to-base64
  ([^String file]
    (let [filename (.getName (io/file file))]
      (with-open [in (io/input-stream file)
                  out (io/output-stream (io/file (str "/tmp/" filename)))]
        (b64/encoding-transfer in out))
        (let [data (slurp (str "/tmp/" filename))]
          (io/delete-file (str "/tmp/" filename))
          data))))