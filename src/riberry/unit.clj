(ns riberry.unit
  (:require [std.lib :as h :refer [defimpl]]
            [std.string :as str]
            [malli.core :as mc]))

(defmulti unit-invoke
  "invokes a given unit"
  {:added "0.1"}
  :op)

(defmethod unit-invoke :default
  [m]
  (into {} m))

(defn unit-display
  "displays a given unit"
  {:added "0.1"}
  [{:keys [id]
    :as m}]
  (let [{:keys [tags]} m]
    (conj tags id)))

(defn unit-string
  "formats the display string"
  {:added "0.1"}
  [m]
  (str "#riberry.unit" (unit-display m)))

(defimpl Unit [id method path tags
               path-params
               query-params
               body-params
               body-upload]
  :type defrecord
  :invoke [unit-invoke 1]
  :string unit-string
  :final true)
