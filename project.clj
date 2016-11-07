(defproject fhirvc "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.cli "0.3.5"]
                 [hiccup "1.0.5"]
                 [yogthos/config "0.8"]
                 [cheshire "5.6.3"]
                 [json-html "0.4.0"]
                 [me.raynes/fs "1.4.6"]
                 [ring "1.5.0"]]                  
  :plugins [[lein-ring "0.9.7"]]
  :ring {:handler fhirvc.handler/app}
  :main fhirvc.core
  :profiles
  {:dev {:dependencies [[im.chit/vinyasa "0.4.7"]] :resource-paths ["config/dev"]}
   :prod {:resource-paths ["config/prod"]}})
