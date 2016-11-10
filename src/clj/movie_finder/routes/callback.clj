(ns movie-finder.routes.callback
  (:require [compojure.core :refer [defroutes GET POST]]
            [ring.util.http-response :as response]
            [movie-finder.config :refer [env]]
            [movie-finder.bots.network :refer [post-messenger]]))

;; ========================== WebToken validation =============================
(defn validate-webhook-token
  "Validate query-params map according to user's defined webhook-token.
  Return hub.challenge if valid, error message else."
  [params]
  (if (and (= (params "hub.mode") "subscribe")
           (= (params "hub.verify_token") (:webhooks-verify-token env)))
    (params "hub.challenge")
    (response/bad-request! "Verify token not valid")))

;; ========================== WIP - Data simulation ===========================

(def statefull_database {:1303278973030229
                         {:current_context {:id :date_context
                                            :current_action :filter_movies_by_category}}})
;; Context
(def context_by_date {:id :date_context
                      :actions [:filter_movies_by_date :filter_movies_by_category]})

;; Actions
;; Deal with all data filtering, validation, API calls
;; Is not supposed to handle user discussion
(def actions
  {:filter_movies_by_date {:validation_fn (fn [input] true)
                           :action_fn     (fn [input] (println "Action Find Movies By Date"))
                           :helper_fn     :filter_movies_by_date_template}
   :filter_movies_by_category {:validation_fn (fn [input]
                                                (if (some #{input} #{"Action" "Comedy"})
                                                  true))
                               :action_fn     (fn [input]
                                                [{:title "Tarzan"} {:title "Batman"}])
                               :helper_fn     :filter_movies_by_category_template}})

;; Helpers
(def helpers
  {:filter_movies_by_date_template {:success_fn    (fn [user-id input] "show success")
                                    :error_fn (fn [error] true)
                                    :explanation_fn (fn [user-id input] (println "build some button or text"))}
   :filter_movies_by_category_template {:success_fn (fn [user-id input]
                                                      (post-messenger user-id {:text "This is a success :-)"}))
                                        :error_fn (fn [error] true)
                                        :explanation_fn (fn [user-id input]
                                                          (post-messenger user-id {:text "Choose a category bewteen Action and Comedy"}))}})


(def entry {:sender {:id 1303278973030229} :recipient {:id 333972820299338} :timestamp 1478766542031 :message {:mid "mid.1478766542031:34f6671b53" :seq 10 :text "test"}})

(defn compute-entry [entry]
  (let [sender-id (keyword (str (get-in entry [:sender :id])))]
    (if-let [sender-status (get statefull_database sender-id)]
      (let [user-input (get-in entry [:message :text])
            current-action (get-in sender-status [:current_context :current_action])
            current-helper (get-in actions [current-action :helper_fn])
            validation-fn (get-in actions [current-action :validation_fn])
            action-fn (get-in actions [current-action :action_fn])
            success-fn (get-in helpers [current-helper :success_fn])
            error-fn (get-in helpers [current-helper :error_fn])
            explanation-fn (get-in helpers [current-helper :explanation_fn])]
        (if (validation-fn user-input)
          (if-let [output (action-fn user-input)]
            (success-fn sender-id output))
          (explanation-fn sender-id user-input)))
      false)))

;; ========================== Webhook Router/Handler ==========================
(defn webhook-router
  "Routes webhook messages to it's processing function based on webhook type.
  Note the Multiple responses are possible for each webhook type based on payload
  or text/attachment... content."
  [entries]
  (dorun
    (map (fn [{messaging :messaging}]
           (dorun
             (map (fn [message]
                    (println message)
                    (compute-entry message)) messaging)))
         entries))
  (response/ok))

;; ========================== Router definition ===============================
(defroutes callback-routes
           (GET "/callback" {params :query-params} (validate-webhook-token params))
           (POST "/callback" {params :body} (webhook-router (:entry params))))





