(ns mandelbrot.render
  (:require
    [mikera.image.core :as i]
    [mikera.image.colours :as colour]))

(defn create-buffer [w h]
  (i/new-image w h))

(defn complex-square [[a b]]
  [(- (* a a) (* b b)) (* 2 a b)])

(defn complex-add [[a b] [c d]]
  [(+ a c) (+ b d)])

(defn complex-norm [[a b]]
  (Math/sqrt (+ (* a a) (* b b))))

(defn view->complex-coords [[x y] [w h] zoom [center-x center-y]]
  [(* (+ (- (/ (* 2.0 x) w) 1.0) center-x) zoom)
   (* (+ (- (/ (* 2.0 y) h) 1.0) center-y) zoom)])

(defn f [z c]
  (let [max-iterations 20]
  (loop [ab z
         iterations 0]
    (if (> iterations max-iterations)
      ab
      (recur (complex-add (complex-square ab) c) (inc iterations))))))

(defn value->color [ab]
  (colour/rgb-from-components (mod (long (min (complex-norm ab) (Long/MAX_VALUE))) 255) 0 0))

(defn draw
  [{:keys [:zoom :center :image] :as state}]
  (let [w (i/width image)
        h (i/height image)
        mandelbrot-map (->> (for [x (range w)]
                              (pmap (fn [y]
                                      [[x y] (f [0.0 0.0] (view->complex-coords [x y] [w h] zoom center))]) (range h)))
                            (apply concat)
                            (into {}))]
    (doseq [x (range w)
            y (range h)]
      (->> (mandelbrot-map [x y])
           (value->color)
           (i/set-pixel image x y)))
    image))
