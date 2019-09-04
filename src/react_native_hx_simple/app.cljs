(ns react-native-hx-simple.app
  (:require
   ["expo" :as ex]
   ["react-native" :as rn]
   ["react" :as react]
   [react-native-hx-simple.db :refer [! sub] :as db]
   [hx.react :as hx :refer [defnc]]
   [hx.hooks :as hooks]
   [shadow.expo :as expo]
   [react-native-hx-simple.firebase :as firebase]))

(defnc Greet []
  (let [name (sub [:get-in :display-name])]
    [:<>
     [rn/Text (str "Name:" name)]
     [rn/Button {:onPress (fn []
                            (println "here2")
                            (! [:display-name] "sdhfkjshd"))
                 :title "aslkdjfj2"}]
     [rn/TextInput {:style {:borderWidth 2}
                    :showSoftInputOnFocus true
                    :editable true
                    :keyboardType "default"
                    :value name
                    :onChangeText (fn [text]
                                    (println "here")
                                    (! [:display-name] text))}]]))

(defnc MyButton [{:keys [title on-press]}]
  [rn/TouchableOpacity {:style {:margin 5
                                :padding 10
                                :background-color "#CCF"}
                        :onPress on-press}
   [rn/Text {:style {:text-align "center"}} title]])

(defnc App []
  [rn/View {:style {:padding 8}}
   [rn/Text "Hello4!"]
   [Greet]
   [rn/Text (sub [:get-in :email])]

   [MyButton {:on-press (fn [] (firebase/facebook-login!))
              :title "Facebook login"}]
   [MyButton {:on-press (fn [] (firebase/add!))
              :title "Add document to firebase"}]
   [MyButton {:title "Create User"
              :on-press (fn [] (firebase/create-user! "alexhenderson@riverford.co.uk" "Password1!"))}]
   [MyButton {:title "Listen"
              :on-press (fn [] (firebase/listen "books" "123"
                                               #(![:person/name] (:title %))))}]
   [MyButton {:title "Send verification email"
              :on-press (fn [] (firebase/send-verification-email!))}]
   [MyButton {:title "Sign in"
              :on-press (fn [] (firebase/sign-in! "alexhenderson@riverford.co.uk" "Password1!"))}]
   [MyButton {:title "Log out"
              :on-press (fn [] (firebase/logout!))}]])

(defn start
  {:dev/after-load true}
  []
  (expo/render-root (hx/f [:provider db/provider [App]])))

(defn init []
  (start)
  (firebase/init!))
