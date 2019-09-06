(ns react-native-hx-simple.local-storage
  (:require ["react-native" :as rn]))

(def async-storage (.-AsyncStorage rn))

(defn async-storage-write
  "Converts `data` to a javascript object and writes to async storage"
  [op]
  (let [{:keys [data on-success on-failure]} op]
    (if (seq data)
      (do (.then (.multiSet async-storage (into-array (for [[k v] data]
                                                        #js [k v]))
                            )
                 (fn [& [js-errors]]
                                        ; (println "wrote")
                   (let [errors (js->clj js-errors :keywordize-keys true?)]
                     (when (and (not errors) on-success)
                       (on-success))
                     (when (and errors on-failure)
                       (on-failure errors)))))
          nil)
      
      (when on-success
        (on-success)))))


(defn async-storage-read
  "Reads the provided keys from async storage."
  [op]
  (let [{:keys [ks on-success on-failure]} op]
    (if (seq ks)
      (do (.multiGet async-storage (clj->js ks)
                     (fn [& [js-errors js-result]]
                       (let [errors (js->clj js-errors :keywordize-keys true)
                             result (into {} (js->clj js-result))]
                         (when (and errors on-failure)
                           (on-failure errors))
                         (when (not errors)
                           (on-success result)))))
          nil
          
          (on-success {})))))

(defn async-storage-remove
  "Removes the provided keys from async storage"
  [op]
  (let [{:keys [ks on-success on-failure]} op]
    (if (seq ks)
      (.multiRemove async-storage (clj->js ks)
             (fn [& [js-errors]]
               (let [errors (js->clj js-errors :keywordize-keys true?)]
                 (when (and errors on-failure)
                   (on-failure errors))
                 (when (and on-success (not errors))
                   on-success))))
      (on-success))))

(defn distinct-by
  "Returns a lazy sequence of the elements of coll removing duplicates of (f item).
  Returns a stateful transducer when no collection is provided."
  {:added "1.0"
   :static true}
  ([f]
   (fn [rf]
     (let [seen (volatile! #{})]
       (fn
         ([] (rf))
         ([result] (rf result))
         ([result input]
          (if (contains? @seen input)
            result
            (do (vswap! seen conj input)
                (rf result input))))))))
  ([f coll]
   (let [step (fn step [xs seen]
                (lazy-seq
                 ((fn [[h :as xs] seen]
                    (when-let [s (seq xs)]
                      (if (contains? seen (f h))
                        (recur (rest s) seen)
                        (cons h (step (rest s) (conj seen (f h)))))))
                  xs seen)))]
     (step coll #{}))))

(defn random-string [length]
  (loop [i length
         s ""]
    (if (zero? i)
      s
      (recur (dec i) (str s (char (+ 50 (rand-int 50))))))))

(defn random-map
  "Generates a random map with provided depth and length"
  [opts]
  (let [{:keys [depth length kfn vfn]
         :or {depth 1
              length 20
              kfn #(str (random-uuid))
              vfn #(random-string 100)}} opts]
    (into {}
          (for [k (range length)]
            [(kfn) (if (> depth 1)
                     (random-map (update opts :depth dec))
                     (vfn))]))))

(defn test-spec [opts]
  (let [{:keys [n]} opts]
    (distinct-by
     :maps-per-key
     (map (fn [x]
            {:number-of-keys x
             :maps-per-key (Math/ceil (/ n x))}) (range 1 (inc n))))))

(defn now-ms []
  (.getTime (js/Date.)))

(def the-test (test-spec {:n 10000}))

(defn gen-data [{:keys [number-of-keys maps-per-key] :as opts}]
  (into {}
        (for [i (range number-of-keys)]
          [(str "n=" i)
           (pr-str (mapv random-map
                         (range maps-per-key)))])))

(defn write [{:keys [number-of-keys maps-per-key on-success] :as opts}]
  (let [start-time (now-ms)
        data (gen-data opts)]
    (async-storage-write {:data data
                          :on-success (fn [] #_(println
                                             "number-of-keys" number-of-keys
                                             "maps-per-key" maps-per-key
                                             "write completed in" (- (now-ms) start-time))
                                        (on-success (- (now-ms) start-time)))})))

(defn read [{:keys [number-of-keys maps-per-key on-success] :as opts}]
  (let [start-time (now-ms)
        data (gen-data opts)]
    (async-storage-read {:ks (keys data)
                         :on-success (fn [] #_(println
                                            "number-of-keys" number-of-keys
                                            "maps-per-key" maps-per-key
                                            "read completed in" (- (now-ms) start-time))
                                       (on-success (- (now-ms) start-time)))})))

(defn run-speed-test
  []
  (doseq [opts (test-spec {:n 100})
          :let [{:keys [number-of-keys maps-per-key]} opts
                data (into {}
                           (for [i (range number-of-keys)]
                             [(str "n=" i)
                              (pr-str (mapv random-map
                                            (range maps-per-key)))]))
                start-time (now-ms)]]
    #_(async-storage-write {:data data
                          :on-success #(println
                                        "number-of-keys" number-of-keys
                                        "maps-per-key" maps-per-key
                                        "write completed in" (- (now-ms) start-time))})

    #_(async-storage-read {:ks (keys data)
                         :on-success #(println
                                       "number-of-keys" number-of-keys
                                       "maps-per-key" maps-per-key
                                       "read completed in" (- (now-ms) start-time))} )
    (comment (time
              (write data))
             (time
              (read (keys data)))
             (time
              (delete (keys data))))
                          ;             (println data)
    nil
    ))
