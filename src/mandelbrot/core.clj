(ns mandelbrot.core
  (:gen-class)
  (:require [mikera.image.core :as i]
            [mikera.image.colours :as colour])
  (:import [javax.swing JFrame]))

(set! *warn-on-reflection* true)

(defn complex-square [[a b]]
  [(- (* a a) (* b b)) (* 2 a b)])

;(defn complex-add-constant [[a b] c]
;  [(+ a c) b])

(defn complex-add [[a b] [c d]]
  [(+ a c) (+ b d)])

(defn complex-norm [[a b]]
  (Math/sqrt (+ (* a a) (* b b))))

(defn view->complex-coords [[x y] [w h] zoom]
  [(* (- (/ (* 2.0 x) w) 1.0) zoom)
   (* (- (/ (* 2.0 y) h) 1.0) zoom)])

(defn f [z c]
  (let [max-iterations 20]
  (loop [ab z
         iterations 0]
    (if (> iterations max-iterations)
      ab
      (recur (complex-add (complex-square ab) c) (inc iterations))))))

(defn value->color [ab]
  (colour/rgb-from-components (mod (long (min (complex-norm ab) (Long/MAX_VALUE))) 255) 0 0))

(defn draw-mandelbrot
  [image]
  (let [zoom 2.0
        w (i/width image)
        h (i/height image)]
    (doseq [x (range w)
            y (range h)]
      (->> (f [0.0 0.0] (view->complex-coords [x y] [w h] zoom))
           (value->color)
           (i/set-pixel image x y)))
    image))

(defn -main
  [& args]
  (let [w 800
        h 800
        ^java.awt.BufferedImage image (i/new-image w h)
        ^javax.swing.JFrame f (doto (JFrame.)
            (.setSize w h)
            (.setVisible true))]
    (draw-mandelbrot image)
    (.drawImage ^java.awt.Graphics2D (.getGraphics f) image ^java.awt.geom.AffineTransform(java.awt.geom.AffineTransform. 1.0 0.0
                                                                       0.0 1.0
                                                                       0.0 0.0) nil)))
