(ns riberry.core
  (:require [net.http :as http]
            [std.lib :as h]
            [std.json :as json]
            [std.string :as str]
            [riberry.meta :as meta]
            [riberry.parse :as p]
            [malli.core :as mc]
            [malli.error :as me]))

(defonce ^:dynamic *site-opts* nil)

(defonce ^:dynamic *site-token* nil)

(defonce ^:dynamic *unit* nil)

(defonce ^:dynamic *unit-id* nil)

(defonce ^:dynamic *path-input* nil)

(defonce ^:dynamic *main-input* nil)

(defonce ^:dynamic *interim* nil)

(defonce ^:dynamic *output* nil)

(defn set-site-opts
  "sets the site options"
  {:added "0.1"}
  [api]
  (alter-var-root #'*site-opts* (fn [_] api)))

(defmacro with-site-opts
  "calls with site options"
  {:added "0.1"}
  [[api] & body]
  `(binding [*site-opts* ~api]
     ~@body))

(defn parse-body
  "parses the http response"
  {:added "0.1"}
  [{:keys [body headers]}]
  (let [{:keys [content-type]} headers]
    (cond (str/starts-with? content-type "application/json")
          (json/read body)
          
          :else body)))

(defn make-url-map
  "makes a url given path and path parameters"
  {:added "0.1"}
  [base-url path path-map]
  (let [path (reduce (fn [p [k v]]
                       (str/replace p
                                    (re-pattern (str "\\{" k "\\}"))
                                    v))
                     path
                     path-map)]
    (str base-url path)))

(defn make-url
  "makes a url given path and path parameters"
  {:added "0.1"}
  [base-url path path-input path-params]
  (make-url-map base-url path
                (into {}
                      (map (fn [{:strs [name]} v]
                             [name v])
                           path-params
                           path-input))))

(defn token-get
  "gets a token from api endpoint"
  {:added "0.1"}
  [{:keys [login password]}
   & [site-opts]]
  (let [{:keys [url]} (merge *site-opts* site-opts)
        token  (-> (http/post
                    (str url "/v1/Authentication/Token")
                    {:headers {"Accept" "application/json"
                               "Content-Type" "application/json"}
                     :body (json/write
                            {:method 0
                             :email login
                             :password password
                             :twoFactorAuthenticationCode "string"})})
                   :body
                   json/read
                   (get "value"))
        _     (alter-var-root #'*site-token* (fn [_] token))]
    token))

(defn token-refresh
  "refreshes a token from api endpoint"
  {:added "0.1"}
  [& [site-opts]]
  (let [{:keys [url token]
         :as site-opts} (merge *site-opts*
                               site-opts)
        token (or token *site-token*)
        new-token  (-> (http/get
                        (str url "/v1/Authentication/RefreshToken")
                        {:headers {"Accept" "application/json"
                                   "Content-Type" "application/json"
                                   "Authorization" (str "Bearer " token)}})
                       :body
                       json/read
                       (get "value"))
        _     (alter-var-root #'*site-token* (fn [_] new-token))]
    new-token))

(defn list-organisations
  "lists all organisations"
  {:added "0.1"}
  [& [site-opts]]
  (let [{:keys [url token]
         :as site-opts} (merge *site-opts*
                               site-opts)
        token (or token *site-token*)]
    
    (-> (http/get
         (str url "/v1/Organisation")
         {:headers {"Accept" "application/json"
                    "Content-Type" "application/json"
                    "Authorization" (str "Bearer " token)}})
        :body
        json/read)))


;;
;; parameter checks
;;

(defn check-path-params-single
  "checks a single path params"
  {:added "0.1"}
  [{:strs [schema]
    :as params} input]
  (let [spec  (mc/from-ast (p/parse-component schema))]
    (when (not (mc/validate spec input))
      (throw (ex-info "Not valid"
                      {:method *unit-id*
                       :reason (me/humanize (mc/explain spec input))})))
    input))

(defn check-path-params
  "checks all path params"
  {:added "0.1"}
  [all-params all-inputs]
  (doall (map check-path-params-single all-params all-inputs)))

(defn check-query-params
  "checks all query params"
  {:added "0.1"}
  [all-params all-inputs]
  (let [spec  (mc/from-ast
               {:type :map
                :keys (h/map-juxt [(fn [m]
                                     (get m "name"))
                                   (fn [{:strs [nullable]
                                         :as m}]
                                     (cond-> {:value (p/parse-component (get m "schema"))}
                                       nullable (assoc-in [:properties :optional] true)))]
                                  all-params)})]
    (when (not (mc/validate spec all-inputs))
      (throw (ex-info "Not valid"
                      {:method *unit-id*
                       :reason (me/humanize (mc/explain spec all-inputs))})))
    all-inputs))

(defn check-body-params
  "checks body params"
  {:added "0.1"}
  [{:strs [content]
    :as params} input]
  (let[{:strs [$ref]
        :as schema}  (get-in content ["application/json" "schema"])
       spec  (cond $ref
                   (meta/to-spec $ref)

                   :else (p/parse-component schema))]
    (when (not (mc/validate spec input))
      (throw (ex-info "Not valid"
                      {:method *unit-id*
                       :reason (me/humanize (mc/explain spec input))})))
    input))

(defn call-api
  "calls the api"
  {:added "0.1"}
  [{:keys [method
           path
           path-params
           query-params
           body-params
           body-upload] :as unit}
   path-input
   main-input
   site-opts]
  (binding [*unit-id* (:id unit)]
    (let [{:keys [url token]
           :as site-opts} (merge *site-opts*
                                 site-opts)
          token (or token *site-token*)
          _     (when (not token)
                  (throw (ex-info "Missing values for token" (or site-opts {}))))
          _     (when (not url)
                  (throw (ex-info "Missing values for url" (or site-opts {}))))
          _     (alter-var-root #'*path-input* (fn [_] path-input))
          _     (alter-var-root #'*main-input* (fn [_] main-input))
          _     (alter-var-root #'*unit*  (fn [_] unit))

          ;;    check path params
          _     (check-path-params path-params
                                   path-input)
          url   (make-url url path path-input path-params)
          _     (when body-upload
                  (throw (ex-info "Not Supported" (into {} unit))))

          ;;       check body or query params
          [body
           suffix] (cond (and (= method :post)
                              body-params)
                         [(do (check-body-params body-params
                                                 main-input)
                              (json/write main-input))
                          ""]
                         
                         query-params
                         [nil
            (do (check-query-params query-params
                                    main-input)
                (str "?" (http/encode-form-params main-input)))]
                         
           :else [nil ""])
          
          response (http/request
                    (str url suffix)
                    {:method method
                     :headers {"Accept" "application/json"
                               "Content-Type" "application/json"
                               "Authorization" (str "Bearer " token)}
                     :body body})]
      response)))

(defn create-api-form
  "creates the api form"
  {:added "0.1"}
  [unit]
  (let [{:keys [id
                path-params
                body-params
                body-upload
                query-params]} unit
        
        path-syms (mapv (comp symbol (fn [m]
                                       (get m "name")))
                        path-params)
        [main-ks
         main-sym]   (cond (and body-params
                                (not body-upload))
                           (let [$ref (get-in body-params ["content"
                                                           "application/json"
                                                           "schema"
                                                           "$ref"])]
                             [(cond $ref
                                    (mapv symbol
                                          (keys (:keys (meta/+registry+
                                                        (last (str/split
                                                               $ref
                                                               #"/"))))))
                                    :else [])
                              'body-input])
                           
                           :else
                           (let [names (mapv (fn [m]
                                               (get m "name"))
                                             query-params)
                                 syms  (some (fn [s]
                                               (neg? (.indexOf s ".")))
                                             names)]
                             [(cond syms
                                    []

                                    :else (mapv symbol syms))
                              'query-input]))]
    `(defn ~(symbol id)
       [~@(if (not-empty path-syms)
            [path-syms]
            [])
        ~(cond (empty? main-ks)
               main-sym

               :else
               {:strs main-ks
                :as main-sym})
        ~'& [~'site-opts]]
       (call-api (meta/+all-lu+ ~id)
                 ~path-syms
                 ~main-sym
                 ~'site-opts))))

(def +functions+
  (mapv (comp eval create-api-form) meta/+all+))
