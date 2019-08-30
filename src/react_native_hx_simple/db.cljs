(ns react-native-hx-simple.db
  (:require [hx.react :as hx]
            [hx.hooks :as hooks]
            ["react" :as react]))

(def app-db (atom {}))

(def context (hx/create-context))

(def provider
  {:context context
   :value app-db})

(defmulti subscription (fn [_ [k & _]] k))

(defmethod subscription :db/get-in
  [db [_ path]]
  (get-in db path))

(defmulti handle-event (fn [db [type & _]] type))

(defmethod handle-event :db/assoc-in
  [db [_ path value]]
  (assoc-in db path value))

(defn useSubscription
  [sub]
  (let [db (hooks/useContext context)
        [result updateResult] (react/useState (subscription @db sub))
        k (gensym)]
    (hooks/useEffect
      (fn []
        (add-watch db k (fn [_ _ _ db]
                          (let [new-result (subscription db sub)]
                            (when (not= new-result result)
                              (updateResult new-result)))))
        (fn []
          (remove-watch db k)))
      #js [sub])
    result))

(defn useDispatch []
  (let [db (hooks/useContext context)]
    (hooks/useCallback (fn [evt] (swap! db #(handle-event % evt))))))

(defn dispatch [evt]
  (swap! app-db #(handle-event % evt)))
