(ns react-native-hx-simple.style
  (:require ["react-native" :as rn])
  (:refer-clojure :exclude [rem]))

(def dimensions (.-Dimensions rn))
(def window (.get dimensions "window"))
(def screen-width (.-width window))
(def rem (if (> screen-width 320) 18 15))
(def font-rem 15)

(defn- kwd
  "Forms a single keyword by concatenating strings or keywords"
  [& strs]
  (keyword (apply str (map #(if (keyword? %) (name %) %) strs))))

(def flex
  "Flex properties"
  {:fg1 {:flexGrow 1}
   :fs0 {:flexShrink 0}
   :fdr {:flexDirection "row"}
   :fdrr {:flexDirection "row-reverse"}
   :fdcr {:flexDirection "column-reverse"}
   :fww {:flexWrap "wrap"}
   :aifs {:alignItems "flex-start"}
   :aic {:alignItems "center"}
   :aife {:alignItems "flex-end"}
   :jcc {:justifyContent "center"}
   :jcfe {:justifyContent "flex-end"}
   :jcsb {:justifyContent "space-between"}
   :jcsa {:justifyContent "space-around"}
   :asfs {:alignSelf "flex-start"}
   :asfe {:alignSelf "flex-end"}
   :asc {:alignSelf "center"}
   :ass {:alignSelf "stretch"}
   :oh {:overflow "hidden"}})

(def spacing
  "Margin and padding properties
   ma0 ... ma10            margin: 0|0.25|0.5|1|2|3|4|5|6|7|8 rem
   ml|mr|mb|mt [0-10]      marginLeft, marginRight, marginBottom, marginTop
   mh [0-10]               marginHorizontal
   mv [0-10]               marginVertical"
  (let [scale [["0" 0]
               ["1" 0.25]
               ["2" 0.5]
               ["3" 1]
               ["4" 2]
               ["5" 3]
               ["6" 4]
               ["7" 5]
               ["8" 6]
               ["9" 7]
               ["10" 8]]]
    (into {}
          (for [[pre k] [[:ma :margin]
                         [:ml :marginLeft]
                         [:mr :marginRight]
                         [:mt :marginTop]
                         [:mb :marginBottom]
                         [:mh :marginHorizontal]
                         [:mv :marginVertical]
                         [:pa :padding]
                         [:pl :paddingLeft]
                         [:pr :paddingRight]
                         [:pt :paddingTop]
                         [:pb :paddingBottom]
                         [:ph :paddingHorizontal]
                         [:pv :paddingVertical]]
                [s fac] scale]
            [(kwd pre s) {k (int (* fac rem))}]))))

(def dims
  "Heights and widths
   h0 ... h16 0-16rem"
  (let [scale [["0" 0]
               ["1" 1]
               ["2" 2]
               ["3" 3]
               ["4" 4]
               ["5" 5]
               ["6" 6]
               ["7" 7]
               ["8" 8]
               ["9" 9]
               ["10" 10]
               ["11" 11]
               ["12" 12]
               ["13" 13]
               ["14" 14]
               ["15" 15]
               ["16" 16]
               ["25" "25%"]
               ["50" "50%"]
               ["75" "75%"]
               ["100" "100%"]]]
    (into {}
          (for [[pre k] [[:h :height]
                         [:w :width]
                         [:max-h :maxHeight]
                         [:max-w :maxWidth]
                         [:min-h :minHeight]
                         [:min-w :minWidth]]
                [s x] scale]
            [(kwd pre s) {k (if (string? x) x (int (* x rem)))}]))))

(def absolute
  "Absolute positioning and offsets
   absolute                     position: absolute
   top|right|bottom|left-0      top|right|bottom|left: 0 rem
                     ... 1                         ... 1 rem
                     ... 2                         ... 2 rem
   absolute-fill                position: absolute, top/left/right/bottom: 0  "
  (let [scale [["0" 0]
               ["1" 1]
               ["2" 2]
               ["3" 3]
               ["4" 4]]]
    (into {:absolute {:position "absolute"}
           :absolute-fill {:position "absolute"
                           :top 0
                           :left 0
                           :right 0
                           :bottom 0}}
          (for [[pre k] [[:top- :top]
                         [:right- :right]
                         [:left- :left]
                         [:bottom- :bottom]]
                [s fac] scale]
            [(kwd pre s) {k (int (* fac rem))}]))))

(def border-width
  "Border width properties
   ba                     borderWidth: 1
   bl|br|bt|bb            borderLeftWidth: 1 | borderRightWidth: 1 ..."
  (let [scale [["0" 0]
               ["1" 1]
               ["2" 2 ]]]
    (into {}
          (for [[pre prop] [[:bw :borderWidth]
                            [:blw :borderLeftWidth]
                            [:brw :borderRightWidth]
                            [:btw :borderTopWidth]
                            [:bbw :borderBottomWidth]]
                [s width] scale]
            [(kwd pre s) {prop width}]))))

(def border-radius
  "Border radius properties
   br0 ... br5            borderRadius: 0|0.125|0.25|0.5|1]2 rem"
  (let [scale [["0" 0]
               ["1" 0.125]
               ["2" 0.25]
               ["3" 0.5]
               ["4" 1]
               ["5" 2]]]
    (into {}
          (for [[s fac] scale]
            [(kwd :br s) {:borderRadius (int (* fac rem))}]))))

(def font-size
  "Font size properties
   f-title 4 rem
   f1 ... f6              fontSize: 3|2.25|1.5|1.25|1|0.875 rem"
  (let [scale [["-title" 4]
               ["1" 3]
               ["2" 2.25]
               ["3" 1.5]
               ["4" 1.25]
               ["5" 1]
               ["6" 0.875]
               ["7" 0.625]]]
    (into {}
          (for [[s fac] scale]
            [(kwd :f s) {:fontSize (int (* fac font-rem))}]))))

(def text-align
  "Text align properties
   tl|tc|tr|tj            textAlign: left|center|right|justify"
  {:tl {:textAlign "left"}
   :tc {:textAlign "center"}
   :tr {:textAlign "right"}
   :tj {:textAlign "justify"}})

(def text-decoration
  "Text decoration line properties"
  {:tdu {:textDecorationLine "underline"
         :textDecorationStyle "solid"}
   :tdl {:textDecorationLine "line-through"
         :textDecorationStyle "solid"}})

(def font-family
  "Font family & weight properties"
  {:ffsb {:fontFamily "WorkSans-SemiBold"}
   :ffm {:fontFamily "WorkSans-Medium"}
   :ffr {:fontFamily "WorkSans-Regular"}
   :ffl {:fontFamily "WorkSans-Light"}
   :fft {:fontFamily "RiverfordTitle-Regular"}})

(def opacity
  "Opacity properties
   o10|20|...|100        opacity: 0.1|0.2|...|1
   o05                   opacity: 0.05
   o025                  opacity: 0.025"
  (let [scale [["025" 0.025]
               ["05" 0.05]
               ["10" 0.1]
               ["20" 0.2]
               ["30" 0.3]
               ["40" 0.4]
               ["50" 0.5]
               ["60" 0.6]
               ["70" 0.7]
               ["80" 0.8]
               ["90" 0.9]
               ["100" 1]]]
    (into {}
          (for [[s o] scale]
            [(kwd :o s) {:opacity o}]))))

(def palette
  {:plum0 "#870049"
   :plum1 "#A30063"
   :moss0 "#488b45"
   :teal0 "#00A9A9"
   :ui0 "#2a2a2a"
   :ui1 "#4a4a4a"
   :ui2 "#b5b5b5"
   :ui3 "#f5f5f5"
   :ui4 "#fbfbfb"
   :ui5 "#ffffff"
   :status0 "#5b9851"
   :status1 "#f5a623"
   :status2 "#b5071c"})

(def colors
  "Font family & weight properties"
  (into {}
        (for [[c hex] palette
              [pre prop] [[nil :color]
                          [:b- :borderColor]
                          [:bg- :backgroundColor]]]
          [(kwd pre c) {prop hex}])))

(def os (.-OS (.-Platform rn)))

(def shadow
  (let [shadow-color "#2a2a2a"
        text-shadow-color "rgba(0,0,0,0.7)"]
    {:tsh (when (= os "ios")
            {:textShadowRadius 5
             :textShadowOffset #js {:width 1 :height 1}
             :textShadowColor text-shadow-color})
     :sh1 {:shadowRadius 2
           :shadowOffset {:height 2
                          :width 0}
           :shadowColor shadow-color
           :shadowOpacity 0.25}
     :sh2 {:shadowRadius 3
           :shadowOffset {:height 2
                          :width 0}
           :shadowColor shadow-color
           :shadowOpacity 0.5}
     :sh1-android {:elevation 2}
     :sh2-android {:elevation 4}}))

(def line-heights
  {:lh0 {:lineHeight 1}
   :lh1 {:lineHeight 1.25}
   :lh2 {:lineHeight 1.5}
   :lh3 {:lineHeight 1.75}})

(def tachyons
  (merge flex
         spacing
         dims
         absolute
         border-width
         border-radius
         font-size
         text-align
         text-decoration
         font-family
         opacity
         colors
         line-heights
         shadow))

(defn s [m-or-k & ks]
  (apply merge
         (if (map? m-or-k)
           m-or-k
           (get tachyons m-or-k))
         (map #(get tachyons %) ks)))
