(defproject aplo "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-3149"]
                 [http-kit "2.1.16"]
                 [compojure "1.2.1"]
                 [ring/ring-devel "1.3.1"]
                 [javax.servlet/servlet-api "2.5"]
                 [selmer "0.7.7"]
                 [org.clojure/data.json "0.2.5"]
                 [org.clojure/data.codec "0.1.0"]
                 [clj-http "1.0.1"]
                 [clojure.jdbc "0.2.2"]
                 [clojure.jdbc/clojure.jdbc-c3p0 "0.3.0"]
                 [org.clojure/java.jdbc "0.3.5"]
                 [mysql/mysql-connector-java "5.1.25"]
                 [hickory "0.5.4"]
                 [digest "1.4.4"]
                 [cljs-ajax "0.3.10"]
                 [jayq "2.5.4"]]
  :main ^:skip-aot aplo.core
  :repl-options {:init-ns aplo.core}
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}}
  :plugins [[lein-cljsbuild "1.0.5"]]
  :cljsbuild {:builds
              {:min {:source-paths ["src/cljs"]
               :compiler {:output-to "resources/public/js/lst.js"
                          :externs ["resources/externs/jquery.js"]
                          :foreign-libs [{:file "resources/externs/Chart.js"
                                          :provides ["cljs.chart"]}
                                         {:file "resources/externs/test.js"
                                          :provides ["cljs.test"]}]
                          :optimizations :advanced}}}})