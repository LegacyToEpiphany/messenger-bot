(ns movie-finder.routes.callback
  (:require [compojure.core :refer [defroutes GET POST]]
            [ring.util.http-response :as response]
            [movie-finder.config :refer [env]]
            [movie-finder.bots.network :refer [post-messenger]]
            [clojure.set :refer [difference]]
            [automat.core :as a]
            [automat.viz :refer [view]]))

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

(def statefull_database (atom {:1303278973030229
                               {:current_context {:id             :date_context
                                                  :current_action :filter_movies_by_category
                                                  :done_actions #{}
                                                  :done_inputs {}}}}))
;; Context
(def date_and_category_context {:id :date_context
                                :actions      #{:filter_movies_by_date :filter_movies_by_category}
                                :helper_id    :context_template})

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
                                                                   (post-messenger user-id {:text "Choose a category bewteen Action and Comedy"}))}
   :context_template {:information_template_fn (fn [user-id]
                                                 (post-messenger user-id {:text "What filter do you want to choose ? A or B"}))}})


(def entry {:sender {:id 1303278973030229} :recipient {:id 333972820299338} :timestamp 1478766542031 :message {:mid "mid.1478766542031:34f6671b53" :seq 10 :text "Action"}})

;; ========================== MESSENGER EXAMPLE ===============================
(def messenger-states
  [(a/* :context-question)
   (a/or
     (a/or [:date-question (a/? :date-question) :date-valid-answer
            [(a/* :context-question)
             (a/or [:category-question :category-valid-answer]
                   :default-context)]]
           [:category-question :category-valid-answer
            [(a/* :context-question)
             (a/or [:date-question :date-valid-answer]
                   :default-context)]])
     :default-context)])

(def f
  (a/compile
    [(a/$ :init)
     (a/interpose-$ :save messenger-states)
     (a/$ :end)]
    {:signal   :messenger-state
     :reducers {:init (fn [m _] (assoc m :messenger-pages []))
                :save (fn [m messenger] (update-in m [:messenger-pages] conj messenger))
                :end  (fn [m _] (assoc m :offer? true))}}))

;; ========================== FSM CONSUMPTION =================================

(def adv (partial a/advance f))
(def fsm (atom
           (-> nil
               (adv {:messenger-state :context-question}))))

;; ========================== FSM TRANSITION FUNCTIONS ========================
(def fsm-fn
  {:context-question      {:action_fn (fn [input] (println "Date or Category ?"))
                           :event_fn  (fn [input] (if (= input "Date")
                                                    :date-question))}
   :date-question         {:action_fn (fn [sender-id] (println "Choose a date"))
                           :event_fn  (fn [input] (if (number? (read-string "1987"))
                                                    :date-valid-answer
                                                    :date-question))}
   :date-valid-answer     {:action_fn (fn [sender-id] (println "Thoses are the film for the date 1987"))
                           :event_fn (fn [input] :context-question)}
   :category-question     {:action_fn (fn [sender-id] (println "Choose a correct category"))
                           :event_fn (fn [input] :category-valid-answer)}
   :category-valid-answer {:action_fn (fn [sender-id] (println "Thoses are the film for the category action"))
                           :event_fn (fn [input] :context-question)}
   :default-context       {:action_fn (fn [sender-id] (println "Thoses are the film for the date 1987"))
                           :event_fn (fn [input] :context-question)}})

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
                    (let [sender-id (get-in message [:sender :id])
                          input (get-in message [:message :text])
                          current-state (:messenger-state (peek (:messenger-pages (:value @fsm))))
                          next-state ((get-in fsm-fn [current-state :event_fn]) input)
                          next-action-fn (get-in fsm-fn [next-state :action_fn])]
                      (try
                        (swap! fsm adv {:messenger-state next-state})
                        (next-action-fn sender-id)
                        (catch Exception e (str "caught exception: " (.getMessage e)))))) messaging)))
         entries))
  (response/ok))

;; ========================== Router definition ===============================
(defroutes callback-routes
           (GET "/callback" {params :query-params} (validate-webhook-token params))
           (POST "/callback" {params :body} (webhook-router (:entry params))))





