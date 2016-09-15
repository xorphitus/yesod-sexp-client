(ns yesod-sexp-client.core
  (:require [ajax.core :refer [GET]]
            [cljs.tools.reader :refer [read-string]]
            [cljs.js :refer [empty-state eval js-eval]]
            [quil.core :as q :include-macros true]
            [quil.middleware :as m]))

(enable-console-print!)

(println "This text is printed from src/yesod-sexp-client/core.cljs. Go ahead and edit it and see reloading in action.")

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:x-ratio 1, :y-ratio 1}))

(defn- eval-str [s]
  (eval (empty-state)
        (read-string s)
        {:eval       js-eval
         :source-map true
         :context    :expr}
        (fn [result] result)))

(defn handler [response]
  (let [result (:value (eval-str response))
        v      (vec result)]
    (reset! app-state {:x-ratio (first  v),
                      :y-ratio (second v)})))

(GET "http://localhost:3000/"
    {:handler handler})

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)

;; quil
(defn setup []
  ; Set frame rate to 30 frames per second.
  (q/frame-rate 30)
  ; Set color mode to HSB (HSV) instead of default RGB.
  (q/color-mode :hsb)
  ; setup function returns initial state. It contains
  ; circle color and position.
  {:color 0
   :angle 0})

(defn update-state [state]
  ; Update sketch state by changing circle color and position.
  {:color (mod (+ (:color state) 0.7) 255)
   :angle (+ (:angle state) 0.1)})

(defn draw-state [state]
  ; Clear the sketch by filling it with light-grey color.
  (q/background 240)
  ; Set circle color.
  (q/fill (:color state) 255 255)
  ; Calculate x and y coordinates of the circle.
  (let [angle (:angle state)
        x (* 150 (:x-ratio @app-state) (q/cos angle))
        y (* 150 (:y-ratio @app-state) (q/sin angle))]
    ; Move origin point to the center of the sketch.
    (q/with-translation [(/ (q/width) 2)
                         (/ (q/height) 2)]
      ; Draw the circle.
      (q/ellipse x y 100 100))))

(q/defsketch hello-quil
  :host "quil-canvas"
  :size [500 500]
  ; setup function called only once, during sketch initialization.
  :setup setup
  ; update-state is called on each iteration before draw-state.
  :update update-state
  :draw draw-state
  ; This sketch uses functional-mode middleware.
  ; Check quil wiki for more info about middlewares and particularly
  ; fun-mode.
  :middleware [m/fun-mode])
