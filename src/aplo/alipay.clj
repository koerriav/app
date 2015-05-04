(ns aplo.alipay
  (:require [digest :refer [md5]]))

(def ^:private base "https://mapi.alipay.com/gateway.do?")
(def ^:private request
  {:service "create_direct_pay_by_user"
   :partner "2088711605801401"
   :_input_charset "utf-8"
   :notify_url "http://app.itrich.cn/alipay/notify"
   :return_url "http://app.itrich.cn/alipay/return"
   :error_url "http://app.itrich.cn/alipay/error"
   :out_trade_no "201503112001"
   :subject "dazhongdianping"
   :payment_type "1"
   :total_fee 0.01
   :seller_id "2088711605801401"})
(def ^:private pkey "3ov4a6oecnrmgx905b0kyq90r7ilb56v")
(defn- create-request [sid sno sname total_fee]
  (let [req (assoc request
    :out_trade_no (str (.getTime (java.util.Date.)) sno)
    :subject sname
    :total_fee total_fee)]
  req))
(defn- request-string [sid sno sname total_fee]
  (reduce #(str %1 "&" %2)
    (sort
      (map (fn [[k v]] (str (name k) "=" v)) (create-request sid sno sname total_fee)))))
(defn alipay-url [{:keys [id no name total_fee]}]
  (let [req-str (request-string id no name total_fee)]
    (str base
      req-str
      "&sign="
      (md5 (str req-str pkey))
      "&sign_type=MD5")))