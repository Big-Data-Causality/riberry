(ns riberry.play
  (:require [malli.core :as mc]
            [std.json :as json]
            [std.lib :as h]
            [std.string :as str]))


(comment

  (def Order
    [:schema
     {:registry {"Country" [:map
                            [:name [:enum "FI" "PO"]]
                            [:neighbors [:vector [:ref "Country"]]]]
                 "Burger" [:map
                           [:name string?]
                           [:description {:optional true} string?]
                           [:origin [:maybe [:ref "Country"]]]
                           [:price pos-int?]]
                 "OrderLine" [:map
                              [:burger [:ref "Burger"]]
                              [:amount int?]]
                 "Order" [:map
                          [:lines [:vector [:ref "OrderLine"]]]
                          [:delivery [:map
                                      [:delivered boolean?]
                                      [:address [:map
                                                 [:street string?]
                                                 [:zip int?]
                                                 [:country [:ref "Country"]]]]]]]}}
     "Order"])
  
  
  (mc/ast Order)
  {:type :schema,
   :child {:type :malli.core/schema, :value "Order"},
   :registry
   {"Country"
    {:type :map,
     :keys
     {:name {:order 0, :value {:type :enum, :values ["FI" "PO"]}},
      :neighbors
      {:order 1,
       :value
       {:type :vector,
        :child {:type :ref, :value "Country"}}}}},
    "Burger"
    {:type :map,
     :keys
     {:name {:order 0, :value {:type string?}},
      :description
      {:order 1,
       :value {:type string?},
       :properties {:optional true}},
      :origin
      {:order 2,
       :value {:type :maybe, :child {:type :ref, :value "Country"}}},
      :price {:order 3, :value {:type pos-int?}}}},
    "OrderLine"
    {:type :map,
     :keys
     {:burger {:order 0, :value {:type :ref, :value "Burger"}},
      :amount {:order 1, :value {:type int?}}}},
    "Order"
    {:type :map,
     :keys
     {:lines
      {:order 0,
       :value
       {:type :vector, :child {:type :ref, :value "OrderLine"}}},
      :delivery
      {:order 1,
       :value
       {:type :map,
        :keys
        {:delivered {:order 0, :value {:type boolean?}},
         :address
         {:order 1,
          :value
          {:type :map,
           :keys
           {:street {:order 0, :value {:type string?}},
            :zip {:order 1, :value {:type int?}},
            :country
            {:order 2,
             :value {:type :ref, :value "Country"}}}}}}}}}}}})


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
