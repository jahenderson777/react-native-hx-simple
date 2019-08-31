(ns react-native-hx-simple.db
  (:require [hx.react :as hx]
            [hx.hooks :as hooks]
            ["react" :as react]))

(def app-db (atom {}))

(def context (hx/create-context))

(def provider {:context context
               :value app-db})

(defmulti subscription (fn [_ [k & _]] k))

(defmethod subscription :get-in [db [_ path]]
  (get-in db path))

(defn useSubscription [sub]
  (let [db (hooks/useContext context)
        [result updateResult] (react/useState (subscription @db sub))
        k (gensym)]
    (hooks/useEffect
      (fn []
        (add-watch db k (fn [_ _ _ db]
                          (let [new-result (subscription db sub)]
                            (when (not= new-result result)
                              (updateResult new-result)))))
        #(remove-watch db k))
      #js [sub])
    result))

(defn ! [path val]
  (swap! app-db assoc-in path val))
