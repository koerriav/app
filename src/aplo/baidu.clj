(ns aplo.baidu
  (:require [clj-http.client :as http]
            [clojure.data.json :as json]
            [clojure.java.io :as io]))
(def ak "loQ3oEcZu9Hsi1oxfvvF7zlw")
(def sk "KQyiahYpMisqgPAj1VUMKfGB1bwk5b77")
(def url-access-token
  "https://openapi.baidu.com/oauth/2.0/token")
(def url-vop
  "http://vop.baidu.com/server_api")
(defn- get-access-token []
  (->>
    (http/post url-access-token
      {:form-params
        {:grant_type "client_credentials"
         :client_id ak :client_secret sk}})
    (:body)
    ((fn [x] (json/read-str x :key-fn keyword)))
    (:access_token)))
; (defn parse-voc [data len]
;   (let [token (get-access-token)]
;     (println token)
;     (->>
;       (http/post url-vop
;         {:body data
;          :content-type "audio/amr;"
;          :content-length (str len)
;          :form-params {:cuid "jkt_vop"
;                        :token token}
;          })
;       (#(do (println %) %))
;       (:body)
;       (#(json/read-str % :key-fn keyword)))))
(defn parse-voc
  ([url callback]
    (let [token (get-access-token)]
      (->>
        (http/post url-vop
          {:content-type :json
           :form-params
            {:format "wavs"
             :rate 8000
             :channel 1 :cuid "jkt_vop" :token token
             :url url
             :callback callback}}))))
  ([]
    (let [token (get-access-token)]
      (->>
        (http/post url-vop
          {:content-type :json
           :form-params
            {:format "wav"
             :rate 8000
             :channel 1 :cuid "jkt_vop" :token token
             :url "http://file.itrich.cn/voc/test.wav"
             :callback "http://app.itrich.cn/get/voc"}}))))
  ([{:keys [data len]}]
    (let [token (get-access-token)]
      (->>
        (http/post url-vop
          {:content-type :json
           :form-params
            {:format "wav"
             :rate 8000
             :channel 1 :cuid "jkt_vop" :token token
             :len len
             :speech data}})))))