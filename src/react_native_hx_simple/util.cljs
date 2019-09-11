(ns react-native-hx-simple.util
  (:require [cljs.pprint :refer [pprint]]))

(defn debug-js-obj [obj]
  (println obj (type obj))
  (pprint (for [prop (js->clj (.getOwnPropertyNames js/Object obj))]
            [prop (type (aget obj prop))])))
