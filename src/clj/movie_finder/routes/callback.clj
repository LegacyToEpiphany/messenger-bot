(ns movie-finder.routes.callback
  (:require [compojure.core :refer [defroutes GET POST]]
            [ring.util.http-response :as response]
            [movie-finder.config :refer [env]]
            [movie-finder.bots.network :refer [post-messenger]]
            [clojure.set :refer [difference]]
            [automat.core :as a]
    ;[automat.viz :refer [view]]
            [movie-finder.bots.api :as api]
            [movie-finder.bots.core :as bots]
            [clojure.core.async :refer [<!!] :as async]
            [movie-finder.bots.spec :as spec]
            [movie-finder.bots.send-api :as send-api]))

;; ========================== WebToken validation =============================
(defn validate-webhook-token
  "Validate query-params map according to user's defined webhook-token.
  Return hub.challenge if valid, error message else."
  [params]
  (if (and (= (params "hub.mode") "subscribe")
           (= (params "hub.verify_token") (:webhooks-verify-token env)))
    (params "hub.challenge")
    (response/bad-request! "Verify token not valid")))

(def entry {:sender {:id 1303278973030229} :recipient {:id 333972820299338} :timestamp 1478766542031 :message {:mid "mid.1478766542031:34f6671b53" :seq 10 :text "1987"}})

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
(def fsm (atom {}))

;; ========================== FSM TRANSITION FUNCTIONS ========================
(def fsm-fn
  {:context-question      {:action_fn (fn [sender-id input]
                                        (post-messenger sender-id {:text "Date ou Category"}))
                           :event_fn  (fn [input] (cond (= "Date" input) :date-question
                                                        (= "Category" input) :category-question))}
   :date-question         {:action_fn (fn [sender-id input]
                                        (post-messenger sender-id {:text "What date are you interested in ?"}))
                           :event_fn  (fn [input] (if (number? (read-string input))
                                                    :date-valid-answer
                                                    :date-question))}
   :date-valid-answer     {:action_fn (fn [sender-id input]
                                        (post-messenger sender-id  {:attachment
                                                                    {:type "template"
                                                                     :payload
                                                                           {:template_type "list"
                                                                            :top_element_style "compact"
                                                                            :elements
                                                                            [{:title "This is the First title"
                                                                              :subtitle "This is a subtitle"
                                                                              :image_url "http://www.w3schools.com/css/trolltunga.jpg"
                                                                              :default_action
                                                                              {:type "web_url"
                                                                               :url "https://techcrunch.com/2016/11/20/a-love-story/"}
                                                                              :buttons
                                                                              [{:type "postback"
                                                                                :title "Button"
                                                                                :payload "DEVELOPER_DEFINED_PAYLOAD"}]}
                                                                             {:title "This is the First title"
                                                                              :subtitle "This is a subtitle"
                                                                              :image_url "http://www.w3schools.com/css/trolltunga.jpg"
                                                                              :default_action
                                                                              {:type "web_url"
                                                                               :url "https://techcrunch.com/2016/11/20/a-love-story/"}
                                                                              :buttons
                                                                              [{:type "postback"
                                                                                :title "Button"
                                                                                :payload "DEVELOPER_DEFINED_PAYLOAD"}]}
                                                                             {:title "This is the First title"
                                                                              :subtitle "This is a subtitle"
                                                                              :image_url "http://www.w3schools.com/css/trolltunga.jpg"
                                                                              :default_action
                                                                              {:type "web_url"
                                                                               :url "https://techcrunch.com/2016/11/20/a-love-story/"}
                                                                              :buttons
                                                                              [{:type "postback"
                                                                                :title "Button"
                                                                                :payload "DEVELOPER_DEFINED_PAYLOAD"}]}
                                                                             {:title "This is the First title"
                                                                              :subtitle "This is a subtitle"
                                                                              :image_url "http://www.w3schools.com/css/trolltunga.jpg"
                                                                              :default_action
                                                                              {:type "web_url"
                                                                               :url "https://techcrunch.com/2016/11/20/a-love-story/"}
                                                                              :buttons
                                                                              [{:type "postback"
                                                                                :title "Button"
                                                                                :payload "DEVELOPER_DEFINED_PAYLOAD"}]}]
                                                                            :buttons
                                                                            [{:type "postback"
                                                                              :title "List Button"
                                                                              :payload "DEVELOPER_DEFINED_PAYLOAD"}]}}}))
                           :event_fn  :context-question}
   :category-question     {:action_fn (fn [sender-id input]
                                        (post-messenger sender-id {:text "Choose a category bewteen Action and Comedy"}))
                           :event_fn  (fn [input] (if (some #{input} #{"Action" "Comedy"})
                                                    :category-valid-answer))}
   :category-valid-answer {:action_fn (fn [sender-id input]
                                        (post-messenger sender-id {:text (str "You choose a valid category " input)}))
                           :event_fn  :context-question}
   :default-context       {:action_fn (fn [sender-id input] (println "Thoses are the film for the date 1987"))
                           :event_fn  (fn [input] :context-question)}})

;; ========================== Webhook Router/Handler ==========================
;(comment (defn webhook-router
;           "Routes webhook messages to it's processing function based on webhook type.
;           Note the Multiple responses are possible for each webhook type based on payload
;           or text/attachment... content."
;           [entries]
;           (dorun
;             (map (fn [{messaging :messaging}]
;                    (dorun
;                      (map (fn [message]
;                             (let [sender-id (keyword (str (get-in entry [:sender :id])))
;                                   input (get-in message [:message :text])]
;                               (if-not (get @fsm sender-id)
;                                 (swap! fsm update-in [sender-id] adv {:messenger-state :context-question}))
;                               (loop [current-state (:messenger-state (peek (:messenger-pages (:value (get @fsm sender-id)))))
;                                      next-state ((get-in fsm-fn [current-state :event_fn]) input)
;                                      next-action-fn (get-in fsm-fn [next-state :action_fn])]
;                                 (try
;                                   (swap! fsm update-in [sender-id] adv {:messenger-state next-state})
;                                   (next-action-fn sender-id input)
;                                   (catch Exception e (str "caught exception: " (.getMessage e))))
;                                 (let [next-next-state (get-in fsm-fn [next-state :event_fn])]
;                                   (if (keyword? next-next-state)
;                                     (recur next-state
;                                            next-next-state
;                                            (get-in fsm-fn [next-next-state :action_fn]))))))) messaging)))
;                  entries))
;           (response/ok)))

(defn webhook-router
  "Routes webhook messages to it's processing function based on webhook type.
  Note the Multiple responses are possible for each webhook type based on payload
  or text/attachment... content."
  [entries]
  (dorun
    (map (fn [{messaging :messaging}]
           (dorun
             (map (fn [message]
                    (let [sender-id (keyword (str (get-in entry [:sender :id])))
                          input (get-in message [:message :text])]
                      (when (= input "start1")
                        ;(post-messenger sender-id :message {:text "BUTTON TEMPLATE"})
                        ;(post-messenger sender-id :message {:text "7 types de boutons différents: postback/url/call/share/log-in/log-out/buy"})
                        ;(post-messenger sender-id :message {:text "Chaque \"button template\" peut contenir 3 boutons maximum."})
                        ;(post-messenger sender-id :message {:text "================================="})
                        ;(post-messenger sender-id :message {:text "BUTTON TEMPLATE - POSTBACK BUTTON"})
                        ;(post-messenger sender-id :message {:text "================================="})
                        ;(post-messenger sender-id :message {:text "Les boutons de type \"postback\" ont un titre limité à 20 caractères."})
                        ;(post-messenger sender-id :message {:text "Ils contiennent un champ caché pour l'utilisateur renvoyé au serveur."})
                        ;(post-messenger sender-id :message {:text "Celui-ci peut servir à identifier la prochaine action ou à contenir de la data."})
                        ;(post-messenger sender-id :message {:attachment {:type    "template"
                        ;                                                 :payload (send-api/make-button-template "Texte pré-bouton 1"
                        ;                                                                                         (send-api/make-postback-button {:title   "Titre du bouton"
                        ;                                                                                                                         :payload "payload"}))}})
                        ;(post-messenger sender-id :message {:attachment {:type    "template"
                        ;                                                 :payload (send-api/make-button-template "Texte pré-bouton 2"
                        ;                                                                                         (send-api/make-postback-button {:title   "Titre du bouton 1"
                        ;                                                                                                                         :payload "payload"})
                        ;                                                                                         (send-api/make-postback-button {:title   "Titre du bouton 2"
                        ;                                                                                                                         :payload "payload"}))}})
                        ;(post-messenger sender-id :message {:attachment {:type    "template"
                        ;                                                 :payload (send-api/make-button-template "Texte pré-bouton 3"
                        ;                                                                                         (send-api/make-postback-button {:title   "Titre du bouton 1"
                        ;                                                                                                                         :payload "payload"})
                        ;                                                                                         (send-api/make-postback-button {:title   "Titre du bouton 2"
                        ;                                                                                                                         :payload "payload"})
                        ;                                                                                         (send-api/make-postback-button {:title   "Titre du bouton 3"
                        ;                                                                                                                         :payload "payload"}))}})
                        ;(post-messenger sender-id :message {:text "================================="})
                        ;(post-messenger sender-id :message {:text "BUTTON TEMPLATE - CALL BUTTON"})
                        ;(post-messenger sender-id :message {:text "================================="})
                        ;(post-messenger sender-id :message {:text "Le bouton de type \"call\" permet d'appeler directement un numéro prédéfini"})
                        ;(post-messenger sender-id :message {:attachment {:type    "template"
                        ;                                                 :payload (send-api/make-button-template "Appellez notre super call-center"
                        ;                                                                                         (send-api/make-call-button {:title   "Ici le Call Center"
                        ;                                                                                                                     :payload "+33630867395"}))}})

                        (post-messenger sender-id :message {:attachment {:type    "template"
                                                                         :payload (send-api/make-button-template "Voici un bouton de type url - sans l'extension messenger"
                                                                                                                 (send-api/make-url-button
                                                                                                                   {:url                  "http://google.fr"
                                                                                                                    :title                "Navigateur compact"
                                                                                                                    :webview_height_ratio "compact"}))}})
                        (post-messenger sender-id :message {:attachment {:type    "template"
                                                                         :payload (send-api/make-button-template "Voici un bouton de type url - sans l'extension messenger"
                                                                                                                 (send-api/make-url-button
                                                                                                                   {:url                  "http://google.fr"
                                                                                                                    :title                "Navigateur grand"
                                                                                                                    :webview_height_ratio "tall"}))}})
                        (post-messenger sender-id :message {:attachment {:type    "template"
                                                                         :payload (send-api/make-button-template "Voici un bouton de type url - sans l'extension messenger"
                                                                                                                 (send-api/make-url-button
                                                                                                                   {:url                  "http://google.fr"
                                                                                                                    :title                "Navigateur full"
                                                                                                                    :webview_height_ratio "full"}))}})

                        )))
                  messaging)))
         entries))
  (response/ok))

;; ========================== Router definition ===============================
(defroutes callback-routes
           (GET "/callback" {params :query-params} (validate-webhook-token params))
           (POST "/callback" {params :body} (webhook-router (:entry params))))


