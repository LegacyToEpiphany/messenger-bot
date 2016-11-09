(ns movie-finder.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[movie-finder started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[movie-finder has shut down successfully]=-"))
   :middleware identity})
