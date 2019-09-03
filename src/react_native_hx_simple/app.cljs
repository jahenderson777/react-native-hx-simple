(ns react-native-hx-simple.app
  (:require
   ["expo" :as ex]
   ["react-native" :as rn]
   ["react" :as react]
   [react-native-hx-simple.db :refer [!] :as db]
   [hx.react :as hx :refer [defnc]]
   [hx.hooks :as hooks]
   [shadow.expo :as expo]
   [react-native-hx-simple.firebase :as firebase]))

(defnc Greet []
  ;; use React Hooks for state management
  (let [name (db/useSubscription [:get-in [:person/name]])]
    [:<>
     [rn/Text (str "Name:" name)]
     [rn/Button {:onPress (fn []
                            (println "here2")
                            (! [:person/name] "sdhfkjshd"))
                 :title "aslkdjfj2"}]
     [rn/TextInput {:style {:borderWidth 2}
                    :showSoftInputOnFocus true
                    :editable true
                    :keyboardType "default"
                    :value name
                    :onChangeText (fn [text]
                                    (println "here")
                                    (! [:person/name] text))}]]))

(defnc MyButton [text on-press]
  [rn/TouchableOpacity {:style {:margin 13
                                :padding 20
                                :background-color "red"}
                        :onPress on-press}
   [rn/Text {:style {:text-align "center"}} text]])


(defnc App []
  [:provider db/provider
   [rn/View {:style {:padding 8}}
    [rn/Text "Hello2!"]
    [Greet]
    [Greet]

    [rn/Button {:onPress (fn [] (firebase/facebook-login!))
                :title "Facebook login"}]
    [rn/Button {:onPress (fn [] (firebase/add!))
                :title "Add document to firebase"}]
    [MyButton "Create User"
     (fn [] (firebase/create-user! "alexhenderson@riverford.co.uk" "Password1!"))]
    [MyButton "Listen" (fn [] (firebase/listen "books" "123"
                                              #(![:person/name] (:title %))))]]])

(defn start
  {:dev/after-load true}
  []
  (expo/render-root (hx/f [App])))

(defn init []
  (start))
