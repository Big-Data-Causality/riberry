(ns riberry.core-test
  (:use code.test)
  (:require [riberry.core :as core]
            [riberry.meta :as meta]
            [std.string :as str]))

^{:refer riberry.core/set-site-opts :added "0.1"}
(fact "sets the site options"
  ^:hidden
  
  (core/set-site-opts
   {:url "https://qa.api.riberry.health"})
  => {:url "https://qa.api.riberry.health"})

^{:refer riberry.core/with-site-opts :added "0.1"}
(fact "calls with site options"
  ^:hidden
  
  (core/with-site-opts [{:url "https://qa.api.riberry.health"}]
    core/*site-opts*)
  => {:url "https://qa.api.riberry.health"})

^{:refer riberry.core/parse-body :added "0.1"}
(fact "parses the http response"
  ^:hidden

  (core/parse-body
   {:body (std.json/write {:a 1 :b 2})
    :headers {:content-type "application/json"}})
  => {"a" 1, "b" 2})

^{:refer riberry.core/make-url-map :added "0.1"}
(fact "makes a url given path and path parameters"
  ^:hidden
  
  (core/make-url-map
   "https://qa.api.riberry.health"
   "/v1/Member/Group/{groupId}/User/{userId}/Add"
   {"groupId" "G000"
    "userId"  "User001"})
  => "https://qa.api.riberry.health/v1/Member/Group/G000/User/User001/Add",)

^{:refer riberry.core/make-url :added "0.1"}
(fact "makes a url given path and path parameters"
  ^:hidden
  
  (core/make-url
   "https://qa.api.riberry.health"
   "/v1/Member/Group/{groupId}/User/{userId}/Add"
   ["G000" "User001"]
   [{"name" "groupId"}
    {"name" "userId"}])
  => "https://qa.api.riberry.health/v1/Member/Group/G000/User/User001/Add",)

^{:refer riberry.core/token-get :added "0.1"}
(fact "gets a token from api endpoint"
  ^:hidden
  
  (core/token-get
   {:login "test0001@zcaudate.xyz",
    :password "Acb8642793"})
  => string?)

^{:refer riberry.core/token-refresh :added "0.1"}
(fact "refreshes a token from api endpoint"
  ^:hidden
  
  (core/token-refresh)
  => string?)

^{:refer riberry.core/list-organisations :added "0.1"}
(fact "lists all organisations"
  ^:hidden
  
  (core/list-organisations)
  => (contains-in
      {"totalItemCount" integer?
       "items" [map?]}))

^{:refer riberry.core/check-path-params-single :added "0.1"}
(fact "checks a single path params"
  ^:hidden
  
  (core/check-path-params-single
   {"x-position" 1
    "schema" {"type" "string"}
    "name" "actorId"
    "required" true
    "in" "path"}
   "HelloWorld")
  => "HelloWorld"

  (core/check-path-params-single
   {"x-position" 1
    "schema" {"type" "string"}
    "name" "actorId"
    "required" true
    "in" "path"}
   1234)
  => (throws-info {:reason ["should be a string"]} ))

^{:refer riberry.core/check-path-params :added "0.1"}
(fact "checks all path params"
  ^:hidden

  (core/check-path-params
   [{"x-position" 1
     "schema" {"type" "string"}
     "name" "actorId"
     "required" true
     "in" "path"}]
   ["HelloWorld"])
  => ["HelloWorld"]
  
  (core/check-path-params
   [{"x-position" 1
     "schema" {"type" "string"}
     "name" "actorId"
     "required" true
     "in" "path"}]
   [1234])
  => (throws-info {:reason ["should be a string"]} ))

^{:refer riberry.core/check-query-params :added "0.1"}
(fact "checks all query params"
  ^:hidden

  (core/check-query-params
   (:query-params (meta/+all-lu+
                   "assessment-attempt-get-all-for-actor"))
   
   {"Search" "hello"})
  => (throws-info
      {:reason
       {"Skip" ["missing required key"]
        "Take" ["missing required key"]
        "moduleId" ["missing required key"]}})
  
  (core/check-query-params
   [{"x-position" 1
     "schema" {"type" "string"}
     "name" "actorId"
     "required" true
     "in" "path"}]
   {"actorId" "HelloWorld"})
  => {"actorId" "HelloWorld"})

^{:refer riberry.core/check-body-params :added "0.1"}
(fact "checks body params"
  ^:hidden

  (core/check-body-params
   (:body-params (meta/+all-lu+
                  "program-update"))
   {"name" "hello"
    "version" 123})
  => {"name" "hello", "version" 123}

  (core/check-body-params
   (:body-params (meta/+all-lu+
                  "program-update"))
   {})
  => (throws-info 
      {:reason
       {"name" ["missing required key"],
        "version" ["missing required key"]}}))

^{:refer riberry.core/call-api :added "0.1"}
(fact "calls the api"
  ^:hidden
  
  (core/call-api (meta/+all-lu+ "authentication-sign-in")
                 []
                 {
                  "email" "test0001@zcaudate.xyz"}
                 {})
  => (throws-info
      {:method "authentication-sign-in",
       :reason {"method" ["missing required key"]}})

  (core/call-api (meta/+all-lu+ "authentication-sign-in")
                 []
                 {"method" "Password"
                  "email" "test0001@zcaudate.xyz"}
                 {})
  => (contains-in
      {:status 400,
       :body "{\"errors\":[{\"name\":\"Summary\",\"message\":\"The password is required.\"}]}",}))

^{:refer riberry.core/create-api-form :added "0.1"}
(fact "creates the api form"
  ^:hidden

  (core/create-api-form (meta/+all-lu+ "user-contact-email-template"))
  => '(clojure.core/defn
        user-contact-email-template
        [query-input & [site-opts]]
        (riberry.core/call-api
         (riberry.meta/+all-lu+ "user-contact-email-template")
         []
         query-input
         site-opts))

  (core/create-api-form (meta/+all-lu+ "member-add"))
  => '(clojure.core/defn
        member-add
        [[groupId userId]
         {:strs [message displayId labelIds notify additionalRoles],
          :as body-input}
         &
         [site-opts]]
        (riberry.core/call-api
         (riberry.meta/+all-lu+ "member-add")
         [groupId userId]
         body-input
         site-opts))

  (core/create-api-form (meta/+all-lu+  "survey-content-publish" ))
  => '(clojure.core/defn
        survey-content-publish
        [[contentId] query-input & [site-opts]]
        (riberry.core/call-api
         (riberry.meta/+all-lu+ "survey-content-publish")
         [contentId]
         query-input
         site-opts))

  (core/create-api-form (meta/+all-lu+  "authentication-permitted-all" ))
  => '(clojure.core/defn
        authentication-permitted-all
        [[permission] body-input & [site-opts]]
        (riberry.core/call-api
         (riberry.meta/+all-lu+ "authentication-permitted-all")
         [permission]
         body-input
         site-opts))

  (core/create-api-form (meta/+all-lu+ "authentication-sign-in"))
  => '(clojure.core/defn
        authentication-sign-in
        [{:strs [method email password twoFactorAuthenticationCode],
          :as body-input}
         &
         [site-opts]]
        (riberry.core/call-api
         (riberry.meta/+all-lu+ "authentication-sign-in")
         []
         body-input
         site-opts)))
  



(comment

  
  
  (get meta/+registry+
       "AuthenticationMethod")
  {:type :enum, :values ["Password" "OneTimePasscode"]}
  
  (get meta/+registry+
       "AuthenticationSettings")
  {:type :map,
   :keys
   {"method"
    {:value
     {:type :or,
      :children [{:type :ref, :value "AuthenticationMethod"}]}},
    "email" {:value {:type :string, :properties {:min 1}}},
    "password"
    {:value {:type :string}, :properties {:optional true}},
    "twoFactorAuthenticationCode"
    {:value {:type :string}, :properties {:optional true}}}}
  
  )
