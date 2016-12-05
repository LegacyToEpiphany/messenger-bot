(ns movie-finder.bots.settings
  (:require [clj-http.client :as client]
            [cheshire.core :refer [generate-string]]
            [movie-finder.config :refer [env]]))

(def ^:private facebook-graph-url "https://graph.facebook.com/v2.6")
(def ^:private thread-settings-uri "/me/thread_settings")

(defn get-user-profile
  "Delete any greeting text for a bot given a facebook access token"
  [user-id]
  (client/get (str facebook-graph-url "/" user-id "?fields=first_name,last_name,profile_pic,locale,timezone,gender&access_token=" (:page-access-token env))
              {:content-type   :json
               :socket-timeout 1000
               :conn-timeout   1000
               :accept         :json}))

;; TODO: Add specs so that message is a string with length <181 characters
(defn create-greating-text
  "Set the greeting text to greeting-message of the bot with a given a facebbok
  access token. Possible personalizations can be created with {{user_first_name}}
  {{user_last_name}} and {{user_full_name}}"
  [greeting-message]
  (let [message (generate-string {:setting_type "greeting"
                                  :greeting     {:text greeting-message}})]
    (client/post (str facebook-graph-url thread-settings-uri "?access_token=" (:page-access-token env))
                 {:body           message
                  :content-type   :json
                  :socket-timeout 1000
                  :conn-timeout   1000
                  :accept         :json})))

(defn delete-greeting-text
  "Delete any greeting text for a bot given a facebook access token"
  []
  (let [message (generate-string {:setting_type "greeting"})]
    (client/delete (str facebook-graph-url thread-settings-uri "?access_token=" (:page-access-token env))
                   {:body           message
                    :content-type   :json
                    :socket-timeout 1000
                    :conn-timeout   1000
                    :accept         :json})))

(defn new-thread-action
  "Set the Get Started button and a callback that will be send back by the postback_received
  webhook."
  [user-defined-payload]
  (let [message (generate-string {:setting_type    "call_to_actions"
                                  :thread_state    "new_thread"
                                  :call_to_actions [{:payload user-defined-payload}]})]
    (client/post (str facebook-graph-url thread-settings-uri "?access_token=" (:page-access-token env))
                 {:body           message
                  :content-type   :json
                  :socket-timeout 1000
                  :conn-timeout   1000
                  :accept         :json})))

(defn delete-new-thread
  "Delete any greeting text for a bot given a facebook access token"
  []
  (let [message (generate-string {:setting_type "call_to_actions"
                                  :thread_state "new_thread"})]
    (client/delete (str facebook-graph-url thread-settings-uri "?access_token=" (:page-access-token env))
                   {:body           message
                    :content-type   :json
                    :socket-timeout 1000
                    :conn-timeout   1000
                    :accept         :json})))

(defn persistent-menu [],,,)
(defn account-linking [],,,)
(defn domain-whitelisting [],,,)
(defn payment [],,,)
