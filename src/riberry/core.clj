(ns riberry.core
  (:require [net.http :as http]
            [std.lib :as h]
            [std.json :as json]))

(comment
  (def +token+
    (-> (http/post "https://qa.api.riberry.health/v1/Authentication/Token"
                   {:headers {"Accept" "application/json"
                              "Content-Type" "application/json"}
                    :body (json/write
                           {:method 0
                            :email "chris.zheng@bigdatacausality.com",
                            :password "Acb8642793",
                            :twoFactorAuthenticationCode "string"})})
        :body
        json/read
        (get "value"))))


(comment
  
  (def +token+
    (-> (http/get "https://qa.api.riberry.health/v1/Authentication/RefreshToken"
                   {:headers {"Accept" "application/json"
                              "Content-Type" "application/json"
                              "Authorization" (str "Bearer " +token+)}})
        :body
        json/read
        (get "value"))))

(comment
  ;;
  ;; How come it is possible to view all the organisations?
  ;;
  (def +orgs+
    (-> (http/get "https://qa.api.riberry.health/v1/Organisation"
                  {:headers {"Accept" "application/json"
                             "Content-Type" "application/json"
                             "Authorization" (str "Bearer " +token+)}})
        :body
        json/read
        ))

  (def +bdc-id+ "e95ebdde-2cd6-4c32-bb83-883db5bde74a")

  )

(comment
  ;;
  ;; What is a Product in the context of the platform?
  ;;
  (def +product+
    (-> (http/get "https://qa.api.riberry.health/v1/Product"
                  {:headers {"Accept" "application/json"
                             "Content-Type" "application/json"
                             "Authorization" (str "Bearer " +token+)}})
        :body
        json/read
        ))
  
  (def +bdc-id+ "e95ebdde-2cd6-4c32-bb83-883db5bde74a"))

(comment
  ;;
  ;; Create Access Token
  ;;
  (def +token-create+
    (http/post "https://qa.api.riberry.health/v1/OrganisationAccessToken"
               {:headers {"Accept" "application/json"
                          "Content-Type" "application/json"
                          "Authorization" (str "Bearer " +token+)}
                :body (json/write
                       {"name" "default",
                        "organisationId" "e95ebdde-2cd6-4c32-bb83-883db5bde74a",
                        "roles" ["admin"]
                        })})))


(comment
  ;;
  ;; Create Organisation
  ;;
  (def +org-create+
    (http/post "https://qa.api.riberry.health/v1/OrganisationAccessToken"
               {:headers {"Accept" "application/json"
                          "Content-Type" "application/json"
                          "Authorization" (str "Bearer " +token+)}
                :body 
                (json/write
                 {"colour" {"red" 0, "green" 0, "blue" 0, "alpha" 0},
                  "portalUri" "string",
                  "privacy" 0,
                  "website" "string",
                  "name" "string",
                  "contactEmail" "user@example.com",
                  "timeZoneId" "GMT",
                  "location" {"address" "string", "longitude" 0, "latitude" 0},
                  "alternativeNames" [],
                  "shortName" "string",
                  "registrationWhitelist" ["string"],
                  "bannerUri" "string",
                  "shifts" [],
                  "logoUri" "string",
                  "cultureCode" "string",
                  "description" "string"})})))
