(ns user
  (:require [mount.core :as mount]
            movie-finder.core))

(defn start []
  (mount/start-without #'movie-finder.core/repl-server))

(defn stop []
  (mount/stop-except #'movie-finder.core/repl-server))

(defn restart []
  (stop)
  (start))


