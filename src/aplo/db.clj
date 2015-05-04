(ns aplo.db
    (:require [clojure.java.jdbc :as j]
              [jdbc.pool.c3p0 :as pool]))
(def db {:subprotocol "mysql"
    :subname "//127.0.0.1:3306/jffx"
    :user "root"
    :password "root"})
(def db_market {:subprotocol "mysql"
    :subname "//127.0.0.1:3306/market"
    :user "root"
    :password "root"})
(def db_spider {:subprotocol "mysql"
    :subname "//127.0.0.1:3306/spider"
    :user "root"
    :password "root"})
(def db_lst (pool/make-datasource-spec {:subprotocol "mysql"
    :subname "//127.0.0.1:3306/loushitong"
    :user "root"
    :password "root"}))
(defn update-blog [m]
  (j/update! db :blog m ["id=?" (:id m)]))
(defn get-advice-list []
  (j/query db ["select (select name from agent where user_id=a.user_id) name,text,date_format(a.date,'%Y/%m/%d %T') date from advice a order by a.date asc"]))
(defn get-reg-agent-num [date]
  {:date
    (.format (java.text.SimpleDateFormat. "yyyy/MM/dd hh:mm:ss") (java.util.Date.))
   :num
    (:num (first (j/query db ["select count(1) num from user where reg_date=? and role=1" date])))
   :total
    (:num (first (j/query db ["select count(1) num from user where role=1"])))
   :snum
    (:num (first (j/query db ["select count(1) num from 微信关注用户表 where state = 1"])))})
(defn get-subject-info [id]
  (first (j/query db_market ["select * from 商品表 where id=?" id])))
(defn save-trade [trade]
  (j/insert! db_market :订单表 trade))
(defn save-film [film]
  (try
    (j/insert! db_spider :film film :transaction? false)
    (catch Exception e (println e))))
(def table-name
  {"11" "成交表_日" "12" "成交表_周" "13" "成交表_月"
   "22" "库存表_周" "23" "库存表_月"  "33" "去化周期"
   "41" "新房行情_日" "42" "新房行情_周" "43" "新房行情_月"
   "51" "全国统计_日" "52" "全国统计_周" "53" "全国统计_月"})
(def id-name
  {"1" "商品房" "2" "住宅" "3" "办公" "4" "商业"})
(defn get-data [{:keys [id type step city mark]}]
  (let [r (j/query db_lst
            [(str "select * from " (get table-name (str type mark)) " where 属性=? and 城市=? order by `日期` desc limit ?,7")
             (get id-name id) city step])]
        {:data r}))
(defn get-broadcast [{:keys [id type mark]}]
  (let [date (:日期 (first (j/query db_lst (str "select 日期 from " (get table-name (str type mark)) " group by 日期 order by 日期 desc"))))
    r (j/query db_lst
            [(str "select * from " (get table-name (str type mark)) " where `日期` = ? and 属性=? order by 城市顺序 asc")
            date (get id-name id)])]
    r))
(defn save-advice [data]
  (try
    (do (j/insert! db_lst :advice data :transaction? false) {:s 1})
    (catch Exception e (do (println e) {:s 0 :err "保存失败!"}))))