(ns riberry.meta
  (:require [malli.core  :as mc]
            [malli.error :as me]
            [riberry.parse :as p]
            [riberry.unit :as unit]
            [std.json :as json]
            [std.lib :as h]
            [std.string :as str]))

(defonce +schema+
  (json/read
   (h/sys:resource-content "api/riberry_1.13.12.json")))

(defn parse-registry
  "parses registry"
  {:added "0.1"}
  [schema]
  (h/map-vals
   p/parse-component
   (get-in +schema+ ["components" "schemas"])))

(defonce +registry+
  (parse-registry +schema+))

(defn to-spec
  "creates a spec"
  {:added "0.1"}
  [component & [registry]]
  (mc/from-ast
   {:type :schema
    :child {:type :ref :value (last (str/split component
                                               #"/"))}
    :registry (or registry
                  +registry+)}))

;;
;; API
;; 

(defonce +paths+
  (vec (sort (keys (get-in +schema+ ["paths"])))))

(defn create-entry
  "creates an entry"
  {:added "0.1"}
  [path [method m]]
  (let [dissoc-fn (fn [m] (dissoc m "in"))
        {:strs [operationId
                tags
                parameters
                requestBody]} m
        params (group-by (fn [{:strs [in]}]
                           in)
                         parameters)
        body-params (not-empty requestBody)
        body-upload (boolean
                     (get-in body-params
                             ["content" "multipart/form-data"]))]
    (unit/->Unit (str/spear-case operationId)
                 (keyword method)
                 path
                 tags
                 (not-empty (mapv dissoc-fn (get params "path")))
                 (not-empty (mapv dissoc-fn (get params "query")))
                 body-params
                 body-upload)))

(defonce +all+
  (mapcat (fn [path]
            (let [spec (get-in +schema+ ["paths" path])]
              (map (partial create-entry path) spec)))
          +paths+))

(defonce +all-lu+
  (h/map-juxt [:id identity] +all+))

;;
;; summary checks
;;

(defn list-unit-upload
  []
  (filter (fn [{:keys [path-params
                       query-params
                       body-params]}]
            (and body-params
                 (not (get-in body-params  ["content" "application/json" "schema"]))))
          +all+))

(defn list-unit-triple
  []
  (filter (fn [{:keys [path-params
                       query-params
                       body-params]}]
            (and path-params
                 query-params
                 body-params))
          +all+))


;;
;; malli checks
;;

(defn check-path-params-single
  "checks a single path params"
  {:added "0.1"}
  [{:strs [schema]
    :as params} input]
  (let [spec  (mc/from-ast (p/parse-component schema))]
    (when (not (mc/validate spec input))
      (throw (ex-info "Not valid"
                      {:reason (me/humanize (mc/explain spec input))})))
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
                      {:reason (me/humanize (mc/explain spec all-inputs))})))
    all-inputs))

(defn check-body-params
  "checks body params"
  {:added "0.1"}
  [{:strs [content]
    :as params} input]
  (let[{:strs [$ref]}  (get-in content ["application/json" "schema"])
       spec (to-spec $ref)]
    (when (not (mc/validate spec input))
      (throw (ex-info "Not valid"
                      {:reason (me/humanize (mc/explain spec input))})))
    input))
