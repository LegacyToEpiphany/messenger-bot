(ns movie-finder.routes.callback
  (:require [compojure.core :refer [defroutes GET POST]]
            [ring.util.http-response :as response]
            [movie-finder.config :refer [env]]))

(def ^:private facebook-graph-url "https://graph.facebook.com/v2.6")
(def ^:private message-uri "/me/messages")

;; WebToken validation
(defn validate-webhook-token
  "Validate query-params map according to user's defined webhook-token.
  Return hub.challenge if valid, error message else."
  [params]
  (if (and (= (params "hub.mode") "subscribe")
           (= (params "hub.verify_token") (:webhooks-verify-token env)))
    (params "hub.challenge")
    (response/bad-request! "Verify token not valid")))






;; Router definition
(defroutes callback-routes
           (GET "/callback" {params :query-params} (validate-webhook-token params)))