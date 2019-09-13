(ns react-native-hx-simple.macros
  (:require [hx.react :refer [defnc] :as hx]))

(defmacro defnc-m [name args & body]
  {:style/indent [1 :form [1]]}
  `(defnc ~name ~args
     {:wrap [(react/memo)]}
     ~@body))
