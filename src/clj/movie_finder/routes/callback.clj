(ns movie-finder.routes.callback
  (:require [compojure.core :refer [defroutes GET POST]]
            [ring.util.http-response :as response]
            [movie-finder.config :refer [env]]
            [movie-finder.bots.network :refer [post-messenger]]
            [movie-finder.bots.api :as api]
            [movie-finder.bots.core :as bots]
            [movie-finder.actions.core :refer [context routes]]
            [movie-finder.actions.intro :refer [intro-route]]
            [movie-finder.actions.button-template :refer [button-template-route]]
            [movie-finder.actions.generic-template :refer [generic-template-route]]
            [movie-finder.actions.list-template :refer [list-template-route]]
            [clojure.core.async :as async :refer [go chan <! >! <!! >!! close! alts! timeout]]))

;; ========================== WebToken validation =============================
(defn validate-webhook-token
  "Validate query-params map according to user's defined webhook-token.
  Return hub.challenge if valid, error message else."
  [params]
  (if (and (= (params "hub.mode") "subscribe")
           (= (params "hub.verify_token") (:webhooks-verify-token env)))
    (params "hub.challenge")
    (response/bad-request! "Verify token not valid")))

; ========================== Webhook Router/Handler ==========================

(def app-state (atom {}))
(def input-chan (chan 1))

(def fsm (context (routes intro-route
                          button-template-route
                          generic-template-route
                          list-template-route)))

;; rajouter un timeout pour éteindre close la FSM just in case
(defn simple-fsm [chan]
  (go
    (loop [state :start]
      (let [timeout (timeout 100000)
            [entry c] (alts! [chan timeout])]
        (if (= c chan)
          (let [next-state (fsm entry state)]
            (println next-state)
            (if (= next-state :end)
              (do
                (println "This is the end of the FSM with the state : " entry)
                (close! chan)
                (swap! app-state dissoc (keyword (str (get-in entry [:sender :id])))))
              (recur next-state)))
          (println "Timeout..."))))))

(go
  (loop []
    (when-let [input (<! input-chan)]
      (println "input" input)
      (if-let [user ((keyword (str (get-in input [:sender :id]))) @app-state)]
        (>! (:chan user) input)
        (let [c (chan 1)]
          (swap! app-state assoc (keyword (str (get-in input [:sender :id]))) {:chan c})
          (simple-fsm c)
          (>! c input)))
      (recur))))

; ========================== Webhook Router/Handler ==========================
(defn webhook-router
  "Routes webhook messages to it's processing function based on webhook type.
  Note the Multiple responses are possible for each webhook type based on payload
  or text/attachment... content."
  [entries]
  (dorun
    (map (fn [{messaging :messaging}]
           (dorun
             (map (fn [message] (>!! input-chan message)) messaging))) entries))
  (response/ok))

;; ========================== Router definition ===============================
(defroutes callback-routes
           (GET "/callback" {params :query-params} (validate-webhook-token params))
           (POST "/callback" {params :body} (webhook-router (:entry params))))


