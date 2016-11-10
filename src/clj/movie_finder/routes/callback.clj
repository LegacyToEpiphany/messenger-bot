(ns movie-finder.routes.callback
  (:require [compojure.core :refer [defroutes GET POST]]
            [ring.util.http-response :as response]
            [movie-finder.config :refer [env]]))

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
                                            :current_action :filter_movies_by_category}}})
;; Context
(def context_by_date {:id :date_context
                      :actions [:filter_movies_by_date :filter_movies_by_category]})

;; Actions
(def actions {:filter_movies_by_date
              {:action_fn (fn [input] (println "Action Find Movies By Date"))
               :helper_fn :filter_movies_by_date_template}
              :filter_movies_by_category
              {:action_fn (fn [input] (println "Action Find Movies By Category"))
               :helper_fn :filter_movies_by_category_template}})

;; Helpers
(def helpers {:filter_movies_by_date_template
              {:explanation_fn (fn [input] (println "build some button or text"))
               :validation_fn  (fn [input] true)}
              :filter_movies_by_category_template
              {:explanation_fn (fn [input] (println "build some button or text"))
               :validation_fn  (fn [input] false)}})


(def entry {})

;; WIP About how to route everything ^^
;; Questions :
;; A) Qu'est-ce qui se passe si le sender n'existe pas ?
;; B) Comment sauvegarder l'état de l'input
;; C) qu'est-c
(let [sender-id (keyword (str (get-in entry [:sender :id])))]
  (if-let [sender-status (get statefull_database sender-id)]
    (let [current-action (get-in sender-status [:current_context :current_action])
          current-helper (get-in actions [current-action :helper_fn])
          user-input (get-in entry [:message :text])
          validation-fn (get-in helpers [current-helper :validation_fn])
          action-fn (get-in actions [current-action :action_fn])
          explanation-fn (get-in helpers [current-helper :explanation_fn])]
      (if (validation-fn user-input)
        (action-fn user-input)
        (explanation-fn user-input)))
    false))







