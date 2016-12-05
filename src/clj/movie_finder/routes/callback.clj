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
            [clojure.core.async :as async :refer [go chan <! >! <!! >!! close! alts! timeout pub sub]]))

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

(def fsm (context (routes intro-route
                          button-template-route
                          generic-template-route
                          list-template-route)))

(def webhooks (chan 1024))
(def app-state (atom {}))

(declare fsm-process)

(def to-pub (chan 1))
(def p (pub to-pub (fn [entry]
                     (condp #(contains? %2 %1) entry
                       :delivery :delivery
                       :read :read
                       :message :message
                       :postback :postback))))

(go
  (loop []
    (when-let [entry (<! webhooks)]
      (let [sender-id (keyword (str (get-in entry [:sender :id])))]
        (if-not (sender-id @app-state)
          (let [delivery (chan 1)
                read (chan 1)
                message (chan 1)
                postback (chan 1)]
            (swap! app-state assoc sender-id
                   {:delivery delivery
                    :read     read
                    :message  message
                    :postback postback})
            (fsm-process delivery read message postback))))
      (>!! to-pub entry)
      (recur))))

(defn fsm-process
  "State Machine that keeps user's state in memory."
  [delivery read message postback]
  (go
    (loop [state :start]
      (let [[entry c] (alts! [delivery read message postback])]
        (println entry)
        (println state)
        (if (= c message)
          (let [next-state (fsm entry state)]
            (println next-state)
            (if (= next-state :end)
              (comment (do
                         ;; think how we should handle user's termination
                         (close! chan)
                         (swap! app-state dissoc (keyword (str (get-in entry [:sender :id]))))))
              (recur next-state))))))))

(defn read-process
  "Manage Read Inputs"
  []
  (let [c (chan 1)]
    (sub p :delivery c)
    (go
      (loop []
        (when-let [entry (<! c)]
          (let [sender-id (keyword (str (get-in entry [:sender :id])))
                read-chan (get-in @app-state [sender-id :message])]
            (>!! read-chan entry)
            ;; Do anything you need to with read inputs
            (println "Read"))
          (recur))))))

(defn delivery-process
  "Manage Delivery Inputs"
  []
  (let [c (chan 1)]
    (sub p :read c)
    (go
      (loop []
        (when-let [entry (<! c)]
          (let [sender-id (keyword (str (get-in entry [:sender :id])))
                delivery-chan (get-in @app-state [sender-id :message])]
            (>!! delivery-chan entry)
            ;; Do anything you need to with delivery inputs
            (println "Delivery"))
          (recur))))))

(defn message-process
  "Manage Message Inputs"
  []
  (let [c (chan 1)]
    (sub p :message c)
    (go
      (loop []
        (when-let [entry (<! c)]
          ;; Do everything related to this event
          (let [sender-id (keyword (str (get-in entry [:sender :id])))
                message-chan (get-in @app-state [sender-id :message])]
            (>!! message-chan entry))
          ;; Do anything you need to with message inputs
          (println "Message")
          (recur))))))

(defn postback-process
  "Manage Postback Inputs"
  []
  (let [c (chan 1)]
    (sub p :postback c)
    (go
      (loop []
        (when-let [entry (<! c)]
          (let [sender-id (keyword (str (get-in entry [:sender :id])))
                postback-chan (get-in @app-state [sender-id :message])]
            (>!! postback-chan entry)
            (println "Postback"))
          ;; Do anything you want with postback inputs
          (recur))))))

(read-process)
(message-process)
(postback-process)
(delivery-process)

; ========================== Webhook Router/Handler ==========================
(defn webhook-router
  "Routes webhook messages to it's processing function based on webhook type.
  Note the Multiple responses are possible for each webhook type based on payload
  or text/attachment... content."
  [entries]
  (dorun
    (map (fn [{messaging :messaging}]
           (dorun
             (map (fn [message] (>!! webhooks message)) messaging))) entries))
  (response/ok))

;; ========================== Router definition ===============================
(defroutes callback-routes
           (GET "/callback" {params :query-params} (validate-webhook-token params))
           (POST "/callback" {params :body} (webhook-router (:entry params))))


