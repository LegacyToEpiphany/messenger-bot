(ns movie-finder.env
  (:require [selmer.parser :as parser]
            [clojure.tools.logging :as log]
            [movie-finder.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[movie-finder started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[movie-finder has shut down successfully]=-"))
   :middleware wrap-dev})
