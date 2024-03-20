(ns riberry.meta-test
  (:use code.test)
  (:require [riberry.meta :as meta]
            [malli.core :as mc]
            [malli.error :as me]))

^{:refer malli.core/ast :added "0.1"}
(fact "creates ast from templates"
  ^:hidden
  
  (mc/ast
   [:enum "None" "User" "TrainingSession" "Team"])
  => {:type :enum :values ["None" "User" "TrainingSession" "Team"]}
  
  (mc/ast
   (mc/from-ast
    {:type :enum :values ["None" "User" "TrainingSession" "Team"]}))
  => {:type :enum :values ["None" "User" "TrainingSession" "Team"]}


  (mc/ast
   (mc/from-ast
    {:type :map
     :keys
     {:street  {:value {:type :string}}
      :country {:value {:type :enum :values ["FI" "UA"]}}}}))
  => {:type :map
      :keys
      {:street {:order nil :value {:type :string}}
       :country {:order nil :value {:type :enum :values ["FI" "UA"]}}}}

  (mc/ast
   (mc/from-ast
    {:type :schema
     :child {:type :ref :value "ModuleInCourse"}
     :registry meta/+registry+}))
  => map?
  
  (mc/ast
   [:or
    [:int]
    [:string]])
  => {:type :or :children [{:type :int} {:type :string}]}
  
  (mc/ast [:int])
  => {:type :int}

  (mc/ast
   [:schema {:registry {"ping" [:maybe [:tuple [:= "ping"] [:ref "pong"]]]
                        "pong" [:maybe [:tuple [:= "pong"] [:ref "ping"]]]}}
    "ping"])
  => {:type :schema
      :child {:type :malli.core/schema :value "ping"}
      :registry
      {"ping"
       {:type :maybe
        :child
        {:type :tuple
         :children
         [{:type := :value "ping"} {:type :ref :value "pong"}]}}
       "pong"
       {:type :maybe
        :child
        {:type :tuple
         :children
         [{:type := :value "pong"} {:type :ref :value "ping"}]}}}})

^{:refer malli.core/validate :added "0.1"}
(fact "creates ast from templates"
  ^:hidden
  
  (mc/validate
   (mc/from-ast
    {:type :schema
     :child {:type :malli.core/schema :value "ModuleInCourse"}
     :registry (select-keys meta/+registry+ ["ModuleInCourse"])})
   {"id" "hello"
    "open" true})
  => true
  
  (me/humanize
   (mc/explain
    (mc/from-ast
     {:type :schema
      :child {:type :malli.core/schema :value "ModuleInCourse"}
      :registry (select-keys meta/+registry+ ["ModuleInCourse"])})
    {"id" "hello"}))
  => {"open" ["missing required key"]})

^{:refer riberry.meta/+all-lu+ :added "0.1"}
(fact "creates ast from templates"
  ^:hidden
  
  ((get meta/+all-lu+ "member-add"))
  => {:id "member-add",
      :method :post,
      :path "/v1/Member/Group/{groupId}/User/{userId}/Add",
      :tags ["Member"],
      :path-params
      [{"x-position" 1,
        "schema" {"type" "string"},
        "name" "groupId",
        "required" true}
       {"x-position" 2,
        "schema" {"type" "string"},
        "name" "userId",
        "required" true}],
      :query-params nil,
      :body-params
      {"x-position" 3,
       "x-name" "settings",
       "content"
       {"application/json"
        {"schema"
         {"$ref" "#/components/schemas/AddGroupMemberSettings"}}},
       "required" true},
      :body-upload false})

^{:refer riberry.meta/+schema+ :added "0.1"}
(fact "creates ast from templates"
  ^:hidden

  (get-in meta/+schema+ ["paths" (nth meta/+paths+ 0)]) 
  => (contains-in
      {"get"
       {"operationId" "Actor_Get"
        "tags" ["Actor"]
        "parameters"
        [{"x-position" 1
          "schema" {"type" "string"}
          "name" "actorId"
          "required" true
          "in" "path"}]}})
  
  (get-in meta/+schema+ ["paths" (nth meta/+paths+ 1)])
  => (contains-in
      {"post"
       {"operationId" "AssessmentAttempt_Create",
        "tags" ["AssessmentAttempt"],
        "requestBody"
        {"x-position" 1,
         "x-name" "settings",
         "content"
         {"application/json"
          {"schema"
           {"$ref"
            "#/components/schemas/CreateModuleAttemptSettingsOfAssessmentResult"}}},
         "required" true}}})  
  
  (get-in meta/+schema+ ["paths" (nth meta/+paths+ 2)])
  => (contains-in
      {"get"
       {"operationId" "AssessmentAttempt_GetAllForActor"
        "tags" ["AssessmentAttempt"]
        "parameters"
        [{"x-position" 1
          "schema" {"type" "string"}
          "name" "actorId"
          "required" true
          "in" "path"}
         {"x-position" 2
          "schema" {"nullable" true "type" "string"}
          "name" "Search"
          "in" "query"}
         {"x-position" 3
          "schema"
          {"maximum" 2.147483647E9
           "minimum" 0.0
           "type" "integer"
           "format" "int32"}
          "name" "Skip"
          "in" "query"}
         {"x-position" 4
          "schema"
          {"maximum" 1000.0
           "minimum" 0.0
           "type" "integer"
           "format" "int32"}
          "name" "Take"
          "description"
          "The number (0 - 1000 inclusive) of items to get from the API."
          "in" "query"}
         {"x-position" 5
          "schema" {"nullable" true "type" "string"}
          "name" "moduleId"
          "in" "query"}]}}))

^{:refer riberry.meta/parse-registry :added "0.1"}
(fact "parses registry"
  ^:hidden

  (meta/parse-registry meta/+schema+)
  => map?)

^{:refer riberry.meta/to-spec :added "0.1"}
(fact "creates a spec"
  ^:hidden

  (mc/ast
   (meta/to-spec "ModuleInCourse"
                 (select-keys meta/+registry+
                              ["ModuleInCourse"])))
  => {:type :schema,
      :child {:type :ref, :value "ModuleInCourse"},
      :registry
      {"ModuleInCourse"
       {:type :map,
        :keys
        {"id"
         {:order nil,
          :value {:type :string},
          :properties {:optional true}},
         "imageUri"
         {:order nil,
          :value {:type :string},
          :properties {:optional true}},
         "name"
         {:order nil,
          :value {:type :string},
          :properties {:optional true}},
         "type"
         {:order nil,
          :value {:type :string},
          :properties {:optional true}},
         "open" {:order nil, :value {:type :boolean}}}}}})

^{:refer riberry.meta/create-entry :added "0.1"}
(fact "creates an entry"
  ^:hidden
  
  (into {}
        (meta/create-entry "/v1/Actor/{actorId}"
                           ["get"
                            {"operationId" "Actor_Get"
                             "tags" ["Actor"]
                             "parameters"
                             [{"x-position" 1
                               "schema" {"type" "string"}
                               "name" "actorId"
                               "required" true
                               "in" "path"}]}]))
  => {:id "actor-get"
      :method :get
      :path "/v1/Actor/{actorId}"
      :tags ["Actor"]
      :path-params
      [{"x-position" 1
        "schema" {"type" "string"}
        "name" "actorId"
        "required" true}]
      :query-params nil
      :body-params nil
      :body-upload false})

^{:refer riberry.meta/list-unit-upload :added "0.1"}
(fact "lists all upload units"
  ^:hidden

  (mapv :id (meta/list-unit-upload))
  => ["assessment-content-create-from-zip"
      "assessment-content-update-from-zip"
      "book-content-create-from-zip"
      "book-content-update-from-zip"
      "quiz-content-create-from-zip"
      "quiz-content-update-from-zip"
      "revision-content-create-from-zip"
      "revision-content-update-from-zip"
      "video-content-create-from-zip"
      "video-content-update-from-zip"])

^{:refer riberry.meta/list-unit-triple :added "0.1"}
(fact "lists triple param units"
  ^:hidden

  (meta/list-unit-triple)
  => ())

^{:refer riberry.meta/check-path-params-single :added "0.1"}
(fact "checks a single path params"
  ^:hidden
  
  (meta/check-path-params-single
   {"x-position" 1
    "schema" {"type" "string"}
    "name" "actorId"
    "required" true
    "in" "path"}
   "HelloWorld")
  => "HelloWorld"

  (meta/check-path-params-single
   {"x-position" 1
    "schema" {"type" "string"}
    "name" "actorId"
    "required" true
    "in" "path"}
   1234)
  => (throws-info {:reason ["should be a string"]} ))

^{:refer riberry.meta/check-path-params :added "0.1"}
(fact "checks all path params"
  ^:hidden

  (meta/check-path-params
   [{"x-position" 1
     "schema" {"type" "string"}
     "name" "actorId"
     "required" true
     "in" "path"}]
   ["HelloWorld"])
  => ["HelloWorld"]
  
  (meta/check-path-params
   [{"x-position" 1
     "schema" {"type" "string"}
     "name" "actorId"
     "required" true
     "in" "path"}]
   [1234])
  => (throws-info {:reason ["should be a string"]} ))

^{:refer riberry.meta/check-query-params :added "0.1"}
(fact "checks all query params"
  ^:hidden

  (meta/check-query-params
   (:query-params (meta/+all-lu+
                   "assessment-attempt-get-all-for-actor"))
   
   {"Search" "hello"})
  => (throws-info
      {:reason
       {"Skip" ["missing required key"]
        "Take" ["missing required key"]
        "moduleId" ["missing required key"]}})
  
  (meta/check-query-params
   [{"x-position" 1
     "schema" {"type" "string"}
     "name" "actorId"
     "required" true
     "in" "path"}]
   {"actorId" "HelloWorld"})
  => {"actorId" "HelloWorld"})

^{:refer riberry.meta/check-body-params :added "0.1"}
(fact "checks body params"
  ^:hidden

  (meta/check-body-params
   (:body-params (meta/+all-lu+
                  "program-update"))
   {"name" "hello"
    "version" 123})
  => {"name" "hello", "version" 123}

  (meta/check-body-params
   (:body-params (meta/+all-lu+
                  "program-update"))
   {})
  => (throws-info 
      {:reason
       {"name" ["missing required key"],
        "version" ["missing required key"]}}))
