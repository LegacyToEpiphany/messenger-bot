(ns movie-finder.routes.callback
  (:require [compojure.core :refer [defroutes GET POST]]
            [ring.util.http-response :as response]
            [movie-finder.config :refer [env]]))

(def ^:private facebook-graph-url "https://graph.facebook.com/v2.6")
(def ^:private message-uri "/me/messages")

;; ========================== WebToken validation =============================
(defn validate-webhook-token
  "Validate query-params map according to user's defined webhook-token.
  Return hub.challenge if valid, error message else."
  [params]
  (if (and (= (params "hub.mode") "subscribe")
           (= (params "hub.verify_token") (:webhooks-verify-token env)))
    (params "hub.challenge")
    (response/bad-request! "Verify token not valid")))

;; ========================== Webhook Router/Handler ==========================
(defn webhook-router
  "Routes webhook messages to it's processing function based on webhook type.
  Note the Multiple responses are possible for each webhook type based on payload
  or text/attachment... content."
  [entries]
  (dorun
    (map (fn [{messaging :messaging}]
           (dorun
             (map (fn [message] (println message)) messaging)))
         entries))
  (response/ok))

;; ========================== Router definition ===============================
(defroutes callback-routes
           (GET "/callback" {params :query-params} (validate-webhook-token params))
           (POST "/callback" {params :body} (webhook-router (:entry params))))


;; ========================== WIP - Data simulation ===========================

(def statefull_database {:1303278973030229
                         {:current_context {:id :date_context
                                            :current_action :filter_movies_by_date}}})

(def context_by_date {:id :date_context
                      :actions [:filter_movies_by_date]})

(def action_find_movies_by_date {:id        :filter_movies_by_date
                                  :action_fn (fn [input] (println "action Find Movies By Date"))
                                  :helper_fn :filter_movies_by_date_template})

(def helper_find_movies_by_date {:id             :filter_movies_by_date_template
                                 :explanation_fn (fn [] (println "build some button or text"))
                                 :validation_fn  (fn [input] true)})



;; 1) bon user ? => oui (sinon default context mais à voir plus tard)
;; 2) Check le current context
;; 3) Check la current action
;; 4) a) input valide => renvoyer le résultat (enregistrer l'input correct)
;;    b) input non-valide => renvoyer le helper

(def entry {})

(let [sender-id (keyword (str (get-in entry [:sender :id])))]
  (if-let [sender-status (get statefull_database sender-id)]
    (let [current-action (get-in sender-status [:current_context :current_action])
          user-input (get-in entry [:message :text])
          validation-fn (:validation_fn helper_find_movies_by_date)
          action-fn (:action_fn action_find_movies_by_date)
          explanation-fn (:explanation_fn helper_find_movies_by_date)]
      (if (validation-fn user-input)
        (action-fn user-input)
        (explanation-fn user-input)))
    false))







