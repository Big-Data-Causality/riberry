(ns riberry.parse
  (:require [std.json :as json]
            [std.lib :as h]
            [std.string :as str]))

(defmulti parse-component
  "parses component"
  {:added "0.1"}
  (fn [m] (get m "type")))

(defmethod parse-component nil
  [{:strs [$ref
           not
           enum
           const
           oneOf
           anyOf
           allOf]}]
  (cond $ref
        {:type :ref
         :value (last (str/split $ref #"/"))}

        not
        {:type :not
         :children (parse-component not)}

        enum
        {:type :enum
         :values enum}
        
        (or anyOf
            oneOf)
        {:type :or
         :children (mapv parse-component (or anyOf
                                             oneOf))}

        allOf
        {:type :and
         :children (mapv parse-component allOf)}

        :else
        {:type :string}))

(defmethod parse-component "object"
  [{:strs [properties
           nullable]}]
  {:type :map
   :keys (h/map-vals
          (fn [{:strs [nullable]
                :as v}]
            (cond-> {:value (parse-component v)}
              nullable (assoc-in [:properties :optional] true)))
          properties)})

(defmethod parse-component "array"
  [{:strs [items]}]
  {:type :vector
   :child (parse-component (cond (vector? items)
                                 (first items)
                                 
                                 :else items))})

(defmethod parse-component "string"
  [{:strs [minLength
           maxLength]}]
  (cond-> {:type :string}
    minLength (assoc-in [:properties :min] minLength)
    maxLength (assoc-in [:properties :max] maxLength)))

(defmethod parse-component "number"
  [{:strs [nullable
           minimum
           maximum]}]
  (cond-> {:type :double}
    minimum (assoc-in [:properties :min] minimum)
    maximum (assoc-in [:properties :max] maximum)))

(defmethod parse-component "boolean"
  [{:strs [nullable]}]
  {:type :boolean})

(defmethod parse-component "integer"
  [{:strs [x-enumNames
           nullable
           minimum
           maximum]}]
  (cond x-enumNames
        {:type :enum :values x-enumNames}
        
        :else
        (cond-> {:type :int}
          minimum (assoc-in [:properties :min] minimum)
          maximum (assoc-in [:properties :max] maximum))))

