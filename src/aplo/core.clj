(ns aplo.core
  (:gen-class)
  (:use org.httpkit.server
        clojure.set)
  (:require [ring.middleware.reload :refer [wrap-reload]]
            [ring.util.response :refer [redirect]]
            [compojure.route :refer [files not-found resources]]
            [compojure.handler :refer [site]]
            [compojure.core :refer [defroutes GET POST DELETE PUT context ANY]]
            [clojure.data.json :refer [read-str write-str]]
            [clojure.java.io :as io]
            [clojure.data.json :as json]
            [aplo.util :refer [render logger to-base64]]
            [aplo.alipay :refer [alipay-url]]
            [aplo.db :as db]
            [aplo.weixin :as weixin]
            [aplo.spider.ys66 :as spider]
            [selmer.parser :refer [render-file set-resource-path! cache-on! cache-off!]]
            [clojure.java.shell :refer [sh]])
  (:import (java.util.concurrent Executors)))
(set-agent-send-executor! (Executors/newFixedThreadPool 32))
(defonce server (atom nil))
(defonce scan-users (atom []))
(defn update-scan [key identity old new]
  (doseq [user @scan-users]
    (send! user (json/write-str [(peek new)]))))
(defn save-scan [key identity old new]
  (doseq [user @scan-users]
    (send! user (json/write-str [(peek old)]))))
; (add-watch spider/result :echo update-scan)
(defroutes app
  (GET "/" [] (render "/index.html"))
  (GET "/pay/:type/:sid" [type sid]
    (let [subject (db/get-subject-info sid)]
      (redirect (alipay-url subject))))
  (GET "/alipay/:id" [id buyer_email buyer_id is_success out_trade_no payment_type subject trade_no trade_status]
    (let [trade {:buyer_email buyer_email
                 :buyer_id buyer_id
                 :is_success is_success
                 :out_trade_no out_trade_no
                 :payment_type payment_type
                 :subject subject
                 :trade_no trade_no
                 :trade_status trade_status}]
                (try (db/save-trade trade)
                  (case id
                    "return" (render "/alipay_success.html")
                    "notify" "NOTIFY!"
                    "error" "ERROR!")
                  (catch Exception e (redirect "/")))))
  (GET "/access-token/:service" [service]
    (case service
      "weixin" (weixin/get-access-token)
      ""))
  (GET "/server/ip/:service" [service]
    (case service
      "weixin" (weixin/get-server-ip)
      ""))
  (GET "/lst/sjtj" [id type step city mark]
    (println "数据统计:" id type step city mark)
    (json/write-str (db/get-data {:id id :type type :step (Integer. step) :city city :mark (Integer. mark)})))
  (GET "/lst/broad" [id type mark]
    (println "全国数据:" id type mark)
    (json/write-str (db/get-broadcast {:id id :type type :mark (Integer. mark)})))
  (POST "/lst/advice" [dev advice]
    (println "客户意见:" dev advice)
    (json/write-str (db/save-advice {:dev dev :advice advice})))
  (POST "/loop" [id]
    (json/write-str {:s 0 :info "Not Ready!"}))
  (GET "/loop" []
    (render-file "lst.html" nil))
  (POST "/cmd/refresh" [id]
    (case (Integer. id)
      1 (str (:out (sh "sh" "/home/rich/wap.sh")))
      ""))
  (resources "/")
  (not-found "Not Found"))

(defn stop-server []
  (when-not (nil? @server)
    (@server :timeout 100)
    (reset! server nil)))
(defn start-server [port]
  (reset! server
    (run-server
      (->
        (site #'app)
        (wrap-reload))
      {:port port
       :max-body (* 1024 1024 1024)})))
(defn -main
  ([port & args]
    (println (str "aplo No.1 start at " port " !"))
    (start-server (Integer. port)))
  ([]
    (println (str "aplo No.1 start at 11111 !"))
    (start-server 11111)))