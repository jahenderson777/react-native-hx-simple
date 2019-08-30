(ns react-native-hx-simple.firebase
  (:require ["firebase" :as firebase]
            ;["firebase/firestore" :as firestore]
            ["expo-facebook" :as facebook]))

(js/require "firebase/firestore")

(def facebook-app-id "382724075889395")

(def firebase-config
  #js {:apiKey "AIzaSyCnhfVaqgAw8oXoq6BbHFwK1UliPMIItGM"
       :authDomain "https://tuber-f8698.firebaseio.com"
       :databaseURL "https://tuber-f8698.firebaseio.com"
       :storageBucket "tuber-f8698.appspot.com"
       :projectId "tuber-f8698"})

(defonce firebase-app (.initializeApp firebase firebase-config))

(defn auth* [] (.auth firebase))

(defn create-user! [email password]
  (.createUserWithEmailAndPassword (auth*) email password))

(defn current-user []
  (.-currentUser (auth*)))

(defn send-verification-email! []
  (.sendEmailVerification (current-user)))

(defn facebook-login! []
  (.then (.logInWithReadPermissionsAsync facebook facebook-app-id)
         (fn [res errors]
           (let [_ (println errors ":" res)
                 token (:token (js->clj res :keywordize-keys true))
                 _ (println "token = " token)
                 cred (.auth.FacebookAuthProvider.credential firebase token)
                 auth (auth*)]
             (.then (.signInAndRetrieveDataWithCredential auth cred)
                    (fn [& v] (println v)))
             (.onAuthStateChanged auth (fn [user]
                                         (println "auth state changed:" (.-displayName user))
                                         (swap! react-native-hx-simple.db/app-db assoc :person/name (.-displayName user))))
             (println token)
             (println res)))))

(defn logout! []
  (.signOut (auth*)))

(defn add! []
  (let [coll-id "books"
        _ (println  (js/Object.getOwnPropertyNames firebase))
        db (.firestore firebase)
        _ (println "db: " db)
        _ (println  (js/Object.getOwnPropertyNames db))
        coll (.collection db coll-id)
        p (.add coll (clj->js {:title "hap1"}))]
    (.then p (fn [doc-ref]
               (println "doc written" doc-ref)))))




;(js/require "firebase/firestore")

;(defonce firebase-app (ocall firebase "initializeApp" firebase-config))

