(ns movie-finder.bots.network
  (:require [cheshire.core :refer [generate-string]]
            [movie-finder.config :refer [env]]
            [clj-http.client :as client]))

(def ^:private facebook-graph-url "https://graph.facebook.com/v2.6")
(def ^:private message-uri "/me/messages")

;; ========================== Helper Function =================================

(defn post-messenger
  "Helper function that post a message to a given user"
  [user-id key map]
  (client/post (str facebook-graph-url message-uri "?access_token=" (:page-access-token env))
               {:body           (let [body (generate-string {:recipient {:id user-id}
                                                             key map})]
                                  body)
                :content-type   :json
                :socket-timeout 10000
                :conn-timeout   10000
                :accept         :json}))

(defn typing-on
  [user-id]
  (client/post (str facebook-graph-url message-uri "?access_token=" (:page-access-token env))
               {:body           (let [body (generate-string {:recipient {:id user-id}
                                                             :sender_action "typing_on"})]
                                  body)
                :content-type   :json
                :socket-timeout 10000
                :conn-timeout   10000
                :accept         :json}))