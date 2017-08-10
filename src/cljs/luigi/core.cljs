(ns luigi.core
  (:require [reagent.core :as reagent :refer [atom]]
            [secretary.core :as secretary :include-macros true]
            [cljs.spec.alpha :as s]
            [clojure.string :refer [upper-case reverse]]
            [accountant.core :as accountant]))

(def state (atom {:transforms []}))

(defn get-front-page-stories
  []
  [{:title "foo" :score 100 :comments 50}
   {:title "bar" :score 50 :comments 20}
   {:title "bas" :score 20 :coments 10}])

(defn uppercase-story
  [stories]
  (map #(update % :title upper-case) stories))

(defn reverse-story
  [stories]
  (map #(update % :title reverse) stories))

(defn filter-story
  [stories]
  (filter #(< 20 (:score %)) stories))

(defn print-story
  [s]
  (.log js/console (str s)))

(defn show-output
  [o]
  (swap! state #(assoc % :output (str o))))

(defn alert
  [a]
  (js/alert (str a)))

(def sources [{:name "get-front-page-stories"
               :fn get-front-page-stories}])

(def transforms [{:name "uppercase-story"
                  :fn uppercase-story}
                 {:name "filter-story"
                  :fn filter-story}
                 {:name "reverse-story"
                  :fn reverse-story}])

(defn transform-named
  [name]
  (first (filter #(= name (:name %)) transforms)))

(def sinks [{:name "print-story"
             :fn print-story}
            {:name "show-output"
             :fn show-output}
            {:name "alert"
             :fn alert}])

(defn runner
  [source transforms]
  ((apply comp transforms) (source)))

(defn run-state
  [state]
  (runner (:fn (first sources)) (map :fn (:transforms state))))

(defn find-transform
  [name]
  (first (filter #(= name (:name %)) transforms)))

;; -------------------------
;; Views

(defn add-transform
  [s new-fn]
  (update s :transforms #(conj % (find-transform new-fn))))

(defn add-transform!
  [new-fn]
  (swap! state #(add-transform % new-fn)))

(defn add-fn []
  (let [current-fn (atom "Pick Fn")]
    (fn []
      [:div
       [:select {:on-change #(reset! current-fn (-> % .-target .-value))}
        (for [t (cons {:name "Pick Fn" } transforms)]
          [:option {:value (:name t)
                    :key (:name t)} (:name t)])]
       [:button {:disabled (= "Pick Fn" @current-fn)
                 :on-click #(add-transform! @current-fn)}
        "Add"]])))

(defn sources-picker
  []
  [:select
   (for [s sources]
     [:option {:value (:name s)
               :key (:name s)} (:name s)])])

(defn transforms-picker []
  [:div
   (for [xform (:transforms @state)]
     [:div
      {:key (:name xform)}
      [:select
       (for [t transforms]
         [:option {:value (:name t)
                   :key (:name t)} (:name t)])]])])

(defn sinks-picker []
  [:div
   [:select
    (for [s sinks]
      [:option {:value (:name s)
                :key (:name s)} (:name s)])]
   [:button {:on-click #((:fn (first sinks)) (run-state @state))} "Run!"]])

(defn home-page []
  [:div
   [sources-picker]
   [:div (str "source "((:fn (first sources))))]
   [:div (str "transforms" (:transforms @state))]
   [transforms-picker]
   [add-fn]
   [:div (str "output " (run-state @state))]
   [sinks-picker]])

(defn about-page []
  [:div [:h2 "About luigi"]
   [:div [:a {:href "/"} "go to the home page"]]])

;; -------------------------
;; Routes

(def page (atom #'home-page))

(defn current-page []
  [:div [@page]])

(secretary/defroute "/" []
  (reset! page #'home-page))

(secretary/defroute "/about" []
  (reset! page #'about-page))

;; -------------------------
;; Initialize app

(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (accountant/configure-navigation!
   {:nav-handler
    (fn [path]
      (secretary/dispatch! path))
    :path-exists?
    (fn [path]
      (secretary/locate-route path))})
  (accountant/dispatch-current!)
  (mount-root))
