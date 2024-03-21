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
            (get-in body-params  ["content" "multipart/form-data"]))
          +all+))

(defn list-unit-non-ref
  []
  (filter (fn [{:keys [path-params
                       query-params
                       body-params]}]
            (and (get-in body-params  ["content" "application/json" "schema"])
                 (not (get-in body-params  ["content" "application/json" "schema" "$ref"]))))
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
