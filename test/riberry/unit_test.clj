(ns riberry.unit-test
  (:use code.test)
  (:require [riberry.unit :as unit]))

^{:refer riberry.unit/unit-invoke :added "0.1"}
(fact "invokes a given unit"
  ^:hidden
  
  (unit/unit-invoke {:id "a"})
  => {:id "a"})

^{:refer riberry.unit/unit-display :added "0.1"}
(fact "displays a given unit"
  ^:hidden
  
  (unit/unit-display {:id "a"
                      :tags ["class"]})
  => ["class" "a"])

^{:refer riberry.unit/unit-string :added "0.1"}
(fact "formats the display string"
  ^:hidden

  (unit/unit-string {:id "a"
                     :tags ["class"]})
  => "#riberry.unit[\"class\" \"a\"]")
