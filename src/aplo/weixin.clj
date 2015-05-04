(ns aplo.weixin
    (:require [clj-http.client :as http]
              [clojure.data.json :as json]))

(def appid "wx803528e6abe00a04")
(def appsecret "ee3a825863e8fb73e29e361dc3c9200e")
(def access-token (atom nil))
(defn- create-request-url [appid appsecret]
    (str
        "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid="
        appid "&secret=" appsecret))
(defn get-access-token []
    (let [access_token @access-token]
        (cond
            (nil? access_token)
            (let [url (create-request-url appid appsecret)
                  rep (:body (http/get url {:as "UTF-8"}))]
                (let [{:keys [access_token expires_in]} (json/read-str rep :key-fn keyword)]
                    (reset! access-token access_token)
                    (future (do
                        (Thread/sleep (- (* 1000 expires_in) 1000))
                        (reset! access-token nil)))
                    access_token))
            :else access_token)))

(def ^:private req-url "https://api.weixin.qq.com/cgi-bin/getcallbackip?access_token=")
(defn get-server-ip []
    (let [url (str req-url (get-access-token))
          rep (:body (http/get url))]
      (let [{:keys [ip_list]} (json/read-str rep :key-fn keyword)]
        (json/write-str ip_list))))