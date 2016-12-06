(ns movie-finder.routes.callback
  (:require [compojure.core :refer [defroutes GET POST]]
            [ring.util.http-response :as response]
            [movie-finder.config :refer [env]]
            [movie-finder.bots.network :refer [post-messenger]]
            [movie-finder.bots.api :as api]
            [movie-finder.bots.core :as bots]
            [movie-finder.actions.core :refer [context routes]]
            [movie-finder.actions.intro :refer [intro-route]]
            [movie-finder.actions.content :refer [content-route]]
            [movie-finder.actions.button-template :refer [button-template-route]]
            [movie-finder.actions.generic-template :refer [generic-template-route]]
    ;;      [movie-finder.actions.list-template :refer [list-template-route]]
            [clojure.core.async :as async :refer [go chan <! >! <!! >!! close! alts! timeout pub sub sliding-buffer]]))

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
                          content-route
                          button-template-route
                          generic-template-route
                          ;;list-template-route
                          )))

(def webhooks (chan 1024))
(def app-state (atom {}))

(declare fsm-process)

(def to-pub (chan 1024))
(def p (pub to-pub (fn [entry]
                     (println "Topic choosing" entry)
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
          (let [delivery (chan (sliding-buffer 1))
                read (chan (sliding-buffer 1))
                message (chan (sliding-buffer 1))
                postback (chan (sliding-buffer 1))]
            (swap! app-state assoc sender-id
                   {:delivery delivery
                    :read     read
                    :message  message
                    :postback postback})
            (fsm-process delivery read message postback))))
      (println "Put value into to-sub: " entry)
      (>!! to-pub entry)
      (recur))))

(defn fsm-process
  "State Machine that keeps user's state in memory."
  [delivery read message postback]
  (go
    (loop [state :start
           delivery-timestamp (System/currentTimeMillis)
           read-timestamp (System/currentTimeMillis)]

      (println "State: " state)
      (let [[entry c] (alts! [delivery read message postback])]
        (condp = c
          message (let [next-state (fsm entry state :message)]
                    (recur next-state
                           delivery-timestamp
                           read-timestamp))
          delivery (if (<= delivery-timestamp (get-in entry [:delivery :watermark]))
                     (let [next-state (fsm entry state :delivery)]
                       (recur next-state
                              (get-in entry [:delivery :watermark])
                              read-timestamp)))
          read (if (<= read-timestamp (get-in entry [:read :watermark]))
                 (let [next-state (fsm entry state :read)]
                   (recur next-state
                          delivery-timestamp
                          (get-in entry [:read :watermark]))))
          postback (let [next-state (fsm entry state :postback)]
                     (recur next-state
                            delivery-timestamp
                            read-timestamp))
          :default (do
                     (recur state
                            delivery-timestamp
                            read-timestamp)))))))

(defn read-process
  "Manage Read Inputs"
  []
  (let [c (chan 1)]
    (sub p :read c)
    (go
      (loop []
        (when-let [entry (<! c)]
          (let [sender-id (keyword (str (get-in entry [:sender :id])))
                read-chan (get-in @app-state [sender-id :read])]
            (println "READ event has been recieved!")
            (>!! read-chan entry)
            ;; Do anything you need to with read inputs
            (recur)))))))

(defn delivery-process
  "Manage Delivery Inputs"
  []
  (let [c (chan 1)]
    (sub p :delivery c)
    (go
      (loop []
        (when-let [entry (<! c)]
          (let [sender-id (keyword (str (get-in entry [:sender :id])))
                delivery-chan (get-in @app-state [sender-id :delivery])]
            (>!! delivery-chan entry)
            ;; Do anything you need to with delivery inputs
            (recur)))))))

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
                postback-chan (get-in @app-state [sender-id :postback])]
            (println "POSTBACK event has been recieved!")
            (>!! postback-chan entry))
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
  (println "Webhook received")
  (dorun
    (map (fn [{messaging :messaging}]
           (dorun
             (map (fn [message] (>!! webhooks message)) messaging))) entries))
  (response/ok))

;; ========================== Router definition ===============================
(defroutes callback-routes
           (GET "/callback" {params :query-params} (validate-webhook-token params))
           (POST "/callback" {params :body} (webhook-router (:entry params))))


