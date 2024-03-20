(ns riberry.parse-test
  (:use code.test)
  (:require [riberry.parse :as p]
            [riberry.meta :as meta]))

^{:refer riberry.parse/parse-component :added "0.1"}
(fact "parses component"
  ^:hidden

  (p/parse-component
   {"type" "string"})
  => {:type :string}

  (p/parse-component
   {"type" "number"})
  => {:type :double}
  
  (p/parse-component
   {"type" "integer"})
  => {:type :int}

  (p/parse-component
   {"allOf" [{"$ref" "#/components/schemas/AnswerBase"}
             {"properties" {"uri" {"nullable" true, "type" "string"}},
              "type" "object", "additionalProperties" false}]})
  => {:type :and,
      :children
      [{:type :ref, :value "AnswerBase"}
       {:type :map,
        :keys
        {"uri" {:value {:type :string}, :properties {:optional true}}}}]}
  
  (p/parse-component
   {"properties"
    {"id" {"nullable" true, "type" "string"},
     "imageUri" {"nullable" true, "type" "string"},
     "name" {"nullable" true, "type" "string"},
     "type" {"nullable" true, "type" "string"},
     "open" {"type" "boolean"}},
    "type" "object",
    "additionalProperties" false})
  => {:type :map,
      :keys
      {"id" {:value {:type :string}, :properties {:optional true}},
       "imageUri"
       {:value {:type :string}, :properties {:optional true}},
       "name" {:value {:type :string}, :properties {:optional true}},
       "type" {:value {:type :string}, :properties {:optional true}},
       "open" {:value {:type :boolean}}}}
  

  (p/parse-component
   (get-in meta/+schema+ ["components" "schemas"
                          "Actor"]))
  => {:type :map,
      :keys
      {"id" {:value {:type :string}, :properties {:optional true}},
       "imageUri"
       {:value {:type :string}, :properties {:optional true}},
       "name" {:value {:type :string}, :properties {:optional true}},
       "type" {:value {:type :ref, :value "ActorType"}}}}

  (p/parse-component
   (get-in meta/+schema+ ["components" "schemas"
                          "ActorType"]))
  => {:type :enum
      :values ["None" "User" "TrainingSession" "Team"]}


  (p/parse-component
   (get-in meta/+schema+ ["components" "schemas"
                          "ActivateProgramPhaseSettings"]))
  => {:type :map,
      :keys
      {"sponsor"
       {:value {:type :string, :properties {:min 0, :max 100}},
        :properties {:optional true}},
       "consumerId" {:value {:type :string, :properties {:min 1}}},
       "programPhaseId"
       {:value {:type :string, :properties {:min 1}}}}}

  (p/parse-component
   {"properties"
    {"type" {"nullable" true, "type" "string"},
     "questionIndex" {"type" "integer", "format" "int32"}},
    "type" "object",
    "x-abstract" true,
    "additionalProperties" false})
  => {:type :map,
      :keys
      {"type" {:value {:type :string}, :properties {:optional true}},
       "questionIndex" {:value {:type :int}}}}
  
  
  (p/parse-component
   (get-in meta/+schema+ ["components" "schemas" "AnswerBase"]))
  => {:type :map,
      :keys
      {"type" {:value {:type :string}
               :properties {:optional true}},
       "questionIndex" {:value {:type :int}}}}
  
  (p/parse-component
   (get-in meta/+schema+ ["components" "schemas" "FileAnswer"]))
  => {:type :and,
      :children
      [{:type :ref, :value "AnswerBase"}
       {:type :map,
        :keys
        {"uri" {:value {:type :string}, :properties {:optional true}}}}]}
  
  (p/parse-component
   (get-in meta/+schema+ ["components" "schemas" "Observation"]))
  => {:type :map,
      :keys
      {"groupId" {:value {:type :string}, :properties {:optional true}},
       "creatorId"
       {:value {:type :string}, :properties {:optional true}},
       "observerName"
       {:value {:type :string}, :properties {:optional true}},
       "organisationId"
       {:value {:type :string}, :properties {:optional true}},
       "observerId"
       {:value {:type :string}, :properties {:optional true}},
       "groupType"
       {:value {:type :string}, :properties {:optional true}},
       "definitionId"
       {:value {:type :string}, :properties {:optional true}},
       "id" {:value {:type :string}, :properties {:optional true}},
       "groupName"
       {:value {:type :string}, :properties {:optional true}},
       "name" {:value {:type :string}, :properties {:optional true}},
       "answers"
       {:value {:type :vector, :child {:type :string}},
        :properties {:optional true}},
       "contentId"
       {:value {:type :string}, :properties {:optional true}},
       "performed" {:value {:type :string}},
       "version" {:value {:type :int}},
       "publisherId"
       {:value {:type :string}, :properties {:optional true}}}})
