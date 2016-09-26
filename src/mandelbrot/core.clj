(ns mandelbrot.core
  (:gen-class)
  (:require [mikera.image.core :as i]
            [mikera.image.colours :as colour]
            [mandelbrot.render :refer [create-buffer draw]])
  (:import [javax.swing JFrame JOptionPane JPanel]
           [java.awt.event KeyListener KeyEvent]
           [java.awt Dimension]))

;; JFrame code borrowed from here: http://java.ociweb.com/mark/programming/ClojureSnake.html

; (set! *warn-on-reflection* true)


(defn render-frame [{:keys [image ^java.awt.Panel panel]}]
  (.drawImage ^java.awt.Graphics2D (.getGraphics panel) image 
              ^java.awt.geom.AffineTransform(java.awt.geom.AffineTransform. 1.0 0.0
                                                                            0.0 1.0
                                                                            0.0 0.0) nil))

(defn step [{:keys [:zoom :center] :as state} key-state*]
  ;; handle key-presses
  (let [{:keys [:key-code :shift] :as key-state} @key-state*
        [center-x center-y] center
        new-state (condp = key-code
                    KeyEvent/VK_LEFT (assoc state :center [(- center-x 0.25) center-y])
                    KeyEvent/VK_RIGHT (assoc state :center [(+ center-x 0.25) center-y])
                    KeyEvent/VK_UP (assoc state :center [center-x (inc center-y)])
                    KeyEvent/VK_DOWN (assoc state :center [center-x (dec center-y)])
                    KeyEvent/VK_Z (assoc state :zoom (if shift (inc zoom) (dec zoom)))
                    nil)]
    (compare-and-set! key-state* key-state (assoc key-state :key-code nil))
    (if new-state
      (do
        (draw new-state)
        new-state)
      state)))

(defn create-panel [width height key-state*]
  (proxy [JPanel KeyListener]
    [] ; superclass constructor arguments
    (getPreferredSize [] (Dimension. width height))
    (keyPressed [e]
      (let [key-code (.getKeyCode e)]
        (condp = key-code
          KeyEvent/VK_SHIFT (swap! key-state* assoc :shift true)
        (compare-and-set! key-state* @key-state* (assoc @key-state* :key-code key-code)))))
    (keyReleased [e]
      (let [key-code (.getKeyCode e)]
        (condp = key-code
          KeyEvent/VK_SHIFT (swap! key-state* assoc :shift false)
          nil ; do nothing on release of other keys
          )))
    (keyTyped [e]) ; do nothing
  ))

(defn configure-gui [frame panel]
  (doto panel
    (.setFocusable true) ; won't generate key events without this
    (.addKeyListener panel))
  (doto frame
    (.add panel)
    (.pack)
;    (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE)
    (.setVisible true)))

(defn -main [& args]
  (let [w 800
        h 800
        frame (JFrame. "Mandelbrot")
        key-state* (atom {:key-code nil
                          :shift false})
        panel (create-panel w h key-state*)
        image (create-buffer w h)
        state {:image image
               :panel panel
               :zoom 2.0
               :center [0.0 0.0]}]
    (configure-gui frame panel)
    (draw state)
    (loop [state state]
      (render-frame state)
      (Thread/sleep 200)
      (recur (step state key-state*)))))
