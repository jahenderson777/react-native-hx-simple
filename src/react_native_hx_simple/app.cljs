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
  (:require-macros [react-native-hx-simple.macros :refer [defnc-m]]))

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
  [TouchableOpacity (s {:style (merge {:padding 10
                                       :background-color "#CCF"}
                                      (js->clj style))
                        :onPress on-press}
                       :ma1)
   [Text {:style {:text-align "center"}} title]])

(defnc SimpleInput [{:keys [label db-key] :as props}]
  [View (s :w100)
   [Text (s :f6 :pt3) label]
   [TextInput (merge (s {;:style {:borderWidth 2}
                          :showSoftInputOnFocus true
                          :editable true
                          :keyboardType "default"
                          :value (sub2 db-key)
                         :onChangeText #(! [db-key] %)}
                        :w100)
                     props)]])

(def animated-value (.-Value Animated))

(def AnimatedCircle (.createAnimatedComponent Animated Circle))

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
  [Svg {:height 200 :width 200}
   [MyAnimatedCircle {:cx 55;(sub2 :x)
                      :cy 55 ;(sub2 :y)
                      :r 30 :fill "blue"}]])

(defnc MySlider []
  (let [num-keys (or (sub2 :slider) 0)
        maps-per-key (int (/ 1000 num-keys))]
    [Slider {:minimumValue 1
             :maximumValue 1000
             :step 10
             :value num-keys
             :onValueChange #(! [:slider] %)}]))

(defnc MainPage []
  (let [dimensions (db/useDimensions)
        email (sub2 :user-email)
        name (sub2 :user-name)
        book-title (sub2 :book-title)
        max-val 1000
        [test updateTest] (react/useState 999)
        ;num-keys (or (sub2 :slider) 0)
        ;maps-per-key (int (/ 1000 num-keys))
        ]
    (println "MainPage render")

    [View {:style (s :pa4 :pt6)}
     [Text "Hello7"]
     [MySvg]
     [Text test]
     [Text name]
     [Text email]
     [Text book-title]
     [Text (str dimensions)]

     [MySlider]
     [Text (str "num keys  = " num-keys "  maps/key =" maps-per-key)]
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

(defnc SignIn []
  (let [user (sub2 :temp-username)
        pw (sub2 :temp-password)]
    [View (s :pa4 :aic)
     [Text (s :f3)
      "Welcome to the App"]
     (comment [SimpleInput {:label "Username" :db-key :temp-username}]
              [SimpleInput {:label "Password" :db-key :temp-password}])
     (comment [Button1 (s {:title "Sign in"
                           :on-press #(fb/sign-in! user pw)}
                          :mt4 :w9)]
              [Button1 (s {:title "Create User"
                           :on-press #(fb/create-user! user pw)}
                          :mt4 :w9)]
              [Button1 (s {:title "Sign in with Facebook"
                           :on-press #(fb/facebook-login!)}
                          :mt4 :w9)])]))

(defnc App []
  (let [user (sub2 :user-name)]
    (println "user" user)
    (cond (nil? user)
          [SignIn]

          :else
          [MainPage])))



(defn start {:dev/after-load true} []
  (expo/render-root (hx/f [App])))


(defn init []
  (start)
  (db/init!)
  ;(whyDidYouUpdate react)
  (fb/init! (fn [user-info]
              (! [:user-email] (:email user-info)
                 [:user-name] (:name user-info)))))
