(ns riberry.core
  (:require [martian.core :as martian]
            [std.json :as json]))


(martian/bootstrap-openapi "https://qa.api.riberry.health"
                           (slurp "resources/api/riberry_1.13.12.json")
                           )
