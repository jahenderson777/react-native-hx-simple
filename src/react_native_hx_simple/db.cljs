(ns react-native-hx-simple.db
  (:require [hx.react :as hx]
            [hx.hooks :as hooks]
            ["react-native" :refer [Dimensions] :as rn]
            ["react" :as react]))

(def app-db (atom {}))

(def subscriptions (atom {}))

(defn init! []
  (add-watch app-db :subscribers
             (fn [_ _ old-db db]
               (println "watch called2")
               (let [updates
                     (mapcat (fn [[k update-fns]]
                               (let [old-val (get old-db k)
                                     new-val (get db k)]
                                 (when (not= old-val new-val)
                                   (for [f update-fns]
                                     #(f new-val)))))
                             @subscriptions)]
                 (println "updates" updates)
                 (when (seq updates)
                   (rn/unstable_batchedUpdates
                    (fn []
                      (doseq [f updates]
                        (f)))))))))

(defn sub2 [k]
  (let [initial-val (get @app-db k)
        [result updateResult] (react/useState initial-val)]
    (hooks/useEffect
     (fn []
       (swap! subscriptions update k (fn [sub]
                                       (if sub
                                         (conj sub updateResult)
                                         #{updateResult})))
       #(swap! subscriptions dissoc k))
     #js [])
    result))

(def context (hx/create-context))

(def provider {:context context
               :value app-db})

(defmulti subscription (fn [_ [k & _]] k))

(defmethod subscription :get-in [db [_ & path]]
  (get-in db path))

(defn sub [& sub-v]
  (let [db (hooks/useContext context)
        [result updateResult] (react/useState (subscription @db sub-v))
        k (gensym)]
    (hooks/useEffect
      (fn []
        (add-watch db k (fn [_ _ _ db]
                          (let [new-result (subscription db sub-v)]
                            (when (not= new-result result)
                              (updateResult new-result)))))
        #(remove-watch db k))
      #js [sub-v])
    result))



(defmulti handle (fn [_ [k & _]] k))

(defmethod handle :assoc [db [_ & path-vals]]
  {:db (reduce (fn [db [path val]]
                 (assoc-in db path val))
               db
               (partition 2 path-vals))})

(defn ! [& evt]
  (let [{:keys [db] :as ret} (handle @app-db (concat [:assoc] evt))]
    (when (contains? ret :db)
      (reset! app-db db))))



(defn useDimensions
  "A hook that extracts the current window dimenions,
   returns state that will update whenever the dimensions change"
  []
  (let [[dimensions setDimensions] (react/useState (js->clj (.get Dimensions "window")))
        onChange (fn [^js obj]
                   (println "dimensions change" obj)
                   (setDimensions (js->clj (aget obj "window") :keywordize-keys true)))]
    (hooks/useEffect
     (fn []
       (.addEventListener Dimensions "change" onChange)
       #(.removeEventListener Dimensions "change" onChange))
     #js [])
    dimensions))
