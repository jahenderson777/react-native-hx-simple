(ns react-native-hx-simple.app
  (:require ["expo" :as ex]
            ["react-native" :refer [Slider Text View TouchableOpacity TextInput Button
                                    Dimensions Animated] :as rn]
            ["react" :refer [React] :as react]
            ["react-native-svg" :refer [Svg Circle]]
            ["@welldone-software/why-did-you-render" :refer [whyDidYouRender]]
            [hx.react :refer [defnc] :as hx]
            [hx.hooks :as hooks]
            [shadow.expo :as expo]
            [react-native-hx-simple.style :refer [s]]
            [react-native-hx-simple.db :refer [! sub sub2] :as db]
            [react-native-hx-simple.firebase :as fb]
            [react-native-hx-simple.util :as util]
            [react-native-hx-simple.local-storage :as ls])
  (:require-macros [react-native-hx-simple.macros :refer [defui]]))

(defnc Greet []
  (let [name (sub :get-in :user :name)]
    [:<>
     [Text (str "Name:" name)]
     [Button {:onPress (fn [] (! [:user :name] "sdhfkjshd"))
              :title "aslkdjfj2"}]
     [TextInput {:style {:borderWidth 2}
                 :showSoftInputOnFocus true
                 :editable true
                 :keyboardType "default"
                 :value name
                 :onChangeText (fn [text] (! [:user :name] text))}]]))


(defnc Button1 [{:keys [title on-press style]}]
  [TouchableOpacity {:style (merge {:margin 5
                                    :padding 10
                                    :background-color "#CCF"}
                                   (js->clj style))
                     :onPress on-press}
   [Text {:style {:text-align "center"}} title]])

(defnc SimpleInput [{:keys [label db-path] :as props}]
  [View {:style (s :w100)}
   [Text {:style (s :f6 :pt3)} label]
   [TextInput (merge {:style (s {:borderWidth 2}
                                :w100)
                      :showSoftInputOnFocus true
                      :editable true
                      :keyboardType "default"
                      :value (apply sub :get-in db-path)
                      :onChangeText #(! db-path %)}
                     props)]])

(def animated-value (.-Value Animated))

(def AnimatedCircle (.createAnimatedComponent Animated Circle))

(comment
  (def x (animated-value. 40))
  (def y (animated-value. 40))
  (def move! (fn [new-x new-y]
               (-> Animated
                   (.timing x #js {:toValue new-x :duration 1000})
                   (.start))
               (-> Animated
                   (.timing y #js {:toValue new-y :duration 1000})
                   (.start)))))

(defnc MyAnimatedCircle [props]
  (let [{:keys [cx cy]} props
        y (hooks/useIRef (animated-value. 40))
        x (hooks/useIRef (animated-value. 40))
        move! (react/useCallback (fn [new-x new-y]
                                   (-> Animated
                                       (.timing @x #js {:toValue new-x :duration 1000})
                                       (.start))
                                   (-> Animated
                                       (.timing @y #js {:toValue new-y :duration 1000})
                                       (.start)))
                                 #js [])]
    (println "rendering" cx cy)
    (hooks/useEffect
     (fn []
       (move! cx cy))
     #js [cx cy])

    [AnimatedCircle (merge props {:cx @x :cy @y})]))




(defnc MySvg [props]
  (println "MySvg" props)
  [Text "MySvg"]
  #_[Svg {:height 200 :width 200}
   [MyAnimatedCircle {:cx 55;(sub2 :x)
                      :cy 55 ;(sub2 :y)
                      :r 30 :fill "blue"}]])

(defnc MainPage [_]
  (let [;dimensions (db/useDimensions)
        ;email (sub2 :user-email)
        ;name (sub2 :user-name)
        ;book-title (sub2 :book-title)
        max-val 1000
        [test updateTest] (react/useState 999)
        num-keys 10 ;(or (sub2 :slider) 0)
        maps-per-key (int (/ 1000 num-keys))
        ]
    (println "MainPage render")
   ; (def updateTest2 updateTest)
    [View {:style (s :pa4 :pt6)}
     [Text "Hello7"]
     [MySvg]
     [Text test]
     ;[Text name]
     ;[Text email]
     ;[Text book-title]
     ;[Text (str dimensions)]

     #_[Slider {:minimumValue 1
              :maximumValue max-val
              :step 10
              :value num-keys
              :onValueChange #(! [:slider] %)}]
     ;[Text (str "num keys  = " num-keys "  maps/key =" maps-per-key)]
     [Button1 {:on-press (fn [] (ls/write {:number-of-keys num-keys
                                          :maps-per-key maps-per-key
                                          :on-success #(! [:write-time] %)}))
               :title "Write to local storage"}]
     [Text (str "write completed in:" (sub2 :write-time))]
     [Button1 {:on-press (fn [] (ls/read {:number-of-keys num-keys
                                         :maps-per-key maps-per-key
                                         :on-success #(! [:read-time] %)}))
               :title "Read from local storage"}]
     [Text (str "read completed in:" (sub2 :read-time))]
     [Button1 {:on-press #(fb/add!)
               :title "Add document to firebase"}]
     [Button1 {:title "Listen"
               :on-press #(fb/listen "books" "123" (fn [x] (! [:book-title] (:title x))))}]
     [Button1 {:title "Send verification email"
               :on-press #(fb/send-verification-email!)}]
     [Button1 {:title "Log out"
               :on-press #(fb/logout! (! [:user] nil))}]]))

(defnc SignIn [_]
  (let [user (sub2 :temp-username)
        pw (sub2 :temp-password)]
    [View {:style (s :pa4 :aic)}
     [Text {:style (s :f3)}
      "Welcome to the App"]
     [SimpleInput {:label "Username" :db-path [:temp-username]}]
     [SimpleInput {:label "Password" :db-path [:temp-password]}]
     [Button1 {:style (s :mt4 :w9)
               :title "Sign in"
               :on-press #(fb/sign-in! user pw)}]
     [Button1 {:style (s :mt4 :w9)
               :title "Create User"
               :on-press #(fb/create-user! user pw)}]
     [Button1 {:style (s :mt4 :w9)
               :title "Sign in with Facebook"
               :on-press #(fb/facebook-login!)}]]))



(comment (defnc App [_]
           [Foo]
           #_(let [user true;(sub2 :user-name)
                   ]
               (println "user" user)
               (cond (nil? user)
                     [SignIn]

                     :else
                     [MainPage]))))

(defn debug= [a b]
  (println a b (identical? a b))
  (identical? a b))

(defui Baz [props]
  (println "Rendered Baz" props)
  (let [[test updateTest] (react/useState 999)]
    (def bazUpdateTest updateTest)
    [Text {:style (s :ma4 :f2)} (str "Baz" test)]))

(defui Bar [props]
  (println "Rendered Bar" props)
  [View
   [Baz {:b 1}]
   [Text {:style (s :ma4 :f2)} "Bar2"]])


(defui Foo [props]
  (let [[test updateTest] (react/useState 999)]
    (def updateTest2 updateTest)
    (println "Rendered Foo2")
    [View
     [Text (str "foo" test)]
     [Bar {:x 1}]]))


(defn start {:dev/after-load true} []
  (expo/render-root (hx/f [Foo {:a 1}])))


(defn init []
  (start)
  (db/init!)
  ;(whyDidYouUpdate react)
  (fb/init! (fn [user-info]
              (! [:user-email] (:email user-info)
                 [:user-name] (:name user-info)))))
