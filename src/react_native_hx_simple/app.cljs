(ns react-native-hx-simple.app
  (:require ["expo" :as ex]
            ["react-native" :refer [Slider Text View TouchableOpacity TextInput Button] :as rn]
            ["react" :as react]
            [hx.react :refer [defnc] :as hx]
            [shadow.expo :as expo]
            [react-native-hx-simple.style :refer [s]]
            [react-native-hx-simple.db :refer [! sub] :as db]
            [react-native-hx-simple.firebase :as fb]
            [react-native-hx-simple.local-storage :as ls]))

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

(defnc MainPage []
  (let [email (sub :get-in :user :email)
        name (sub :get-in :user :name)
        book-title (sub :get-in :book-title)
        max-val 1000
        num-keys (or (sub :get-in :slider) 0)
        maps-per-key (int (/ 1000 num-keys))]
    [View {:style (s :pa4 :pt6)}
     [Text "Hello3"]
     [Text name]
     [Text email]
     [Text book-title]


     [Slider {:minimumValue 1
              :maximumValue max-val
              :step 10
              :value num-keys
              :onValueChange #(! [:slider] %)}]
     [Text (str "num keys  = " num-keys "  maps/key =" maps-per-key)]
     [Button1 {:on-press (fn [] (ls/write {:number-of-keys num-keys
                                          :maps-per-key maps-per-key
                                          :on-success #(! [:write-time] %)}))
               :title "Write to local storage"}]
     [Text (str "write completed in:" (sub :get-in :write-time))]
     [Button1 {:on-press (fn [] (ls/read {:number-of-keys num-keys
                                         :maps-per-key maps-per-key
                                         :on-success #(! [:read-time] %)}))
               :title "Read from local storage"}]
     [Text (str "read completed in:" (sub :get-in :read-time))]
     [Button1 {:on-press #(fb/add!)
               :title "Add document to firebase"}]
     [Button1 {:title "Listen"
               :on-press #(fb/listen "books" "123" (fn [x] (! [:book-title] (:title x))))}]
     [Button1 {:title "Send verification email"
               :on-press #(fb/send-verification-email!)}]
     [Button1 {:title "Log out"
               :on-press #(fb/logout! (! [:user] nil))}]]))

(defnc SignIn []
  (let [user (sub :get-in :temp-username)
        pw (sub :get-in :temp-password)]
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

(defnc App []
  (let [user (sub :get-in :user)]
    (cond (nil? user)
          [SignIn]

          :else
          [MainPage])))

(defn start {:dev/after-load true} []
  (expo/render-root (hx/f [:provider db/provider [App]])))

(defn init []
  (start)
  (fb/init! (fn [user-info] (! [:user] user-info))))
