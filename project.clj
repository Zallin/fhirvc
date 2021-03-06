(defproject fhirvc "0.1.0-SNAPSHOT"
  :description "FHIR version comparison"
  :url "zallin.github.io/fhirvc/"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.cli "0.3.5"]
                 [hiccup "1.0.5"]
                 [cheshire "5.6.3"]
                 [json-html "0.4.0"]
                 [me.raynes/fs "1.4.6"]
                 [ring "1.5.0"]
                 [hickory "0.7.0"]
                 [environ "1.1.0"]]
  :plugins [[lein-ring "0.9.7"]
            [lein-environ "1.1.0"]]
  :ring {:handler fhirvc.handler/app}
  :main fhirvc.core
  :profiles {:dev {:env {:views-path-prefix "/"}}
             :prod {:env {:views-path-prefix "/fhirvc/"}}})
