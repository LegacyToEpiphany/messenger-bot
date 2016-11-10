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
  {:filter_movies_by_date {:validate_input_fn (fn [input] true)
                           :action_fn         (fn [input] (println "Action Find Movies By Date"))
                           :helper_id         :filter_movies_by_date_template}
   :filter_movies_by_category {:validate_input_fn (fn [input]
                                                (if (some #{input} #{"Action" "Comedy"})
                                                  true))
                               :action_fn         (fn [input]
                                                [{:title "Tarzan"} {:title "Batman"}])
                               :helper_id         :filter_movies_by_category_template}})

;; Helpers
;; Deal with displaying user result, information or error
(def helpers
  {:filter_movies_by_date_template {:result_template_fn      (fn [user-id input] "show success")
                                    :error_template_fn       (fn [user-id error] true)
                                    :information_template_fn (fn [user-id input] (println "build some button or text"))}
   :filter_movies_by_category_template {:result_template_fn      (fn [user-id input]
                                                      (post-messenger user-id {:text "This is a success :-)"}))
                                        :error_template_fn       (fn [user-id error]
                                                                   (post-messenger user-id {:text "I don't understand what you told me ^^"}))
                                        :information_template_fn (fn [user-id input]
                                                                   (post-messenger user-id {:text "Choose a category bewteen Action and Comedy"}))}})


(def entry {:sender {:id 1303278973030229} :recipient {:id 333972820299338} :timestamp 1478766542031 :message {:mid "mid.1478766542031:34f6671b53" :seq 10 :text "test"}})

(defn compute-entry [entry]
  (let [sender-id (keyword (str (get-in entry [:sender :id])))]
    (if-let [sender-status (get statefull_database sender-id)]
      (let [user-input (get-in entry [:message :text])
            current-action (get-in sender-status [:current_context :current_action])
            current-helper (get-in actions [current-action :helper_id])
            validate-input-fn (get-in actions [current-action :validate_input_fn])
            action-fn (get-in actions [current-action :action_fn])
            result-template-fn (get-in helpers [current-helper :result_template_fn])
            error-template-fn (get-in helpers [current-helper :error_template_fn])
            information-template-fn (get-in helpers [current-helper :information_template_fn])]
        (if (validate-input-fn user-input)
          (if-let [output (action-fn user-input)]
            (result-template-fn sender-id output))
          (do
            (error-template-fn sender-id user-input)
            (information-template-fn sender-id user-input))))
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





