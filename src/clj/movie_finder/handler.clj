(ns movie-finder.handler
  (:require [compojure.core :refer [routes wrap-routes]]
            [movie-finder.layout :refer [error-page]]
            [movie-finder.routes.home :refer [home-routes]]
            [movie-finder.routes.callback :refer [callback-routes]]
            [compojure.route :as route]
            [movie-finder.env :refer [defaults]]
            [mount.core :as mount]
            [movie-finder.middleware :as middleware]))

(mount/defstate init-app
                :start ((or (:init defaults) identity))
                :stop  ((or (:stop defaults) identity)))

(def app-routes
  (routes
    (-> #'home-routes
        (wrap-routes middleware/wrap-csrf)
        (wrap-routes middleware/wrap-formats))
    (-> #'callback-routes
        (wrap-routes middleware/wrap-json))
    (route/not-found
      (:body
        (error-page {:status 404
                     :title "page not found"})))))


(defn app [] (middleware/wrap-base #'app-routes))
