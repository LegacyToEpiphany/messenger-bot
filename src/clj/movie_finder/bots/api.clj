(ns movie-finder.bots.api
  (:require [org.httpkit.client :as http]
            [clojure.core.async :refer [chan put! go <!!]]
            [cheshire.core :as cheshire]))

(def apikey "1a36d359b0d2ea513c77951dbe9cf82f")

;; ========================== HELPERS METHOD ==================================
(defn http-get [url]
  (let [c (chan)]
    (println url)
    (http/get url
              (fn [r] (put! c r)))
    c))

(defn request-and-process [url]
  (go
    (-> (str "https://api.themoviedb.org/3/" url "api_key=" apikey)
        http-get
        <!
        :body
        (cheshire/parse-string true)
        )))

;; ========================== DATA RETREIVING =================================
(defn latest-movies []
  (request-and-process "movie/latest?"))

(defn top-rated-movies []
  (request-and-process "movie/top_rated?"))

(defn movie-by-id [id]
  (request-and-process (str "movie/" id "?")))

(defn movie-cast [id]
  (request-and-process (str "movie/" id "/casts?")))

(defn people-by-id [id]
  (request-and-process (str "person/" id "?")))

(defn avg [coll]
  (-> (clojure.core/reduce + 0 coll)
      (/ (count coll))))
