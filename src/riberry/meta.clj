(ns riberry.meta
  (:require [malli.json-schema :as json-schema]
            [malli.json-schema.parse :as parse]
            [std.json :as json]
            [std.lib :as h]))

(def +schema+
  (json/read
   (slurp "resources/api/riberry_1.13.12.json")))

(parse/json-schema-document->malli +schema+)

(comment
  (keys +schema+)
  ("security" "servers" "x-tagGroups" "info" "tags" "paths" "x-logo" "openapi" "x-generator" "components")

  (sort (keys (get +schema+ "paths")))

  (sort (keys (get-in +schema+ ["components" "schemas"]))))

(comment
  (json-schema/select))


(comment
  
  
  "Acb8642793")
