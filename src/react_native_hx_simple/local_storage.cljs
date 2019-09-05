(ns react-native-hx-simple.local-storage
  (:require ["react-native" :as rn]))

(def async-storage (.-AsyncStorage rn))

(defn async-storage-write
  "Converts `data` to a javascript object and writes to async storage"
  [op]
  (let [{:keys [data on-success on-failure]} op]
    (if (seq data)
      (.multiSet async-storage (into-array (for [[k v] data]
                                                    #js [k v]))
             (fn [& [js-errors]]
               (let [errors (js->clj js-errors :keywordize-keys true?)]
                 (when (and (not errors) on-success)
                   (on-success))
                 (when (and errors on-failure)
                   (on-failure errors)))))
      (when on-success
        (on-success)))))

(defn async-storage-read
  "Reads the provided keys from async storage."
  [op]
  (let [{:keys [ks on-success on-failure]} op]
    (if (seq ks)
      (.multiGet async-storage (clj->js ks)
             (fn [& [js-errors js-result]]
               (let [errors (js->clj js-errors :keywordize-keys true)
                     result (into {} (js->clj js-result))]
                 (when (and errors on-failure)
                   (on-failure errors))
                 (when (not errors)
                   (on-success result)))))
      (on-success {}))))

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


(defn run-speed-test
  []
  (doseq [opts (test-spec {:n 10})
          :let [{:keys [number-of-keys maps-per-key]} opts
                data (into {}
                           (for [i (range number-of-keys)]
                             [(str "n=" i)
                              (pr-str (mapv #(random-map {:length maps-per-key})
                                            (range maps-per-key)))]))]]
    
    (comment (time
              (write data))
             (time
              (read (keys data)))
             (time
              (delete (keys data))))
    (println data)))
