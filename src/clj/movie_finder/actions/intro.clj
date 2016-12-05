(ns movie-finder.actions.intro
  (:require [movie-finder.actions.core :refer [messenger-route routes]]
            [movie-finder.bots.network :refer [post-messenger typing-on]]
            [movie-finder.bots.send-api :as send-api]))

(defn intro-action [entry]
  (let [sender-id (keyword (str (get-in entry [:sender :id])))]
    (post-messenger sender-id :message {:text "Bonjour et bienvenue dans ce bot de Showcase !"})))

(defn my-name-action [entry]
  (let [sender-id (keyword (str (get-in entry [:sender :id])))]
    (post-messenger sender-id :message {:text "Je m'appelle Lambda et je vais vous montrez l'ensemble des possibilités offertes par la plateforme Messenger !"})))

(defn your-info-action [entry]
  (let [sender-id (keyword (str (get-in entry [:sender :id])))]
    (post-messenger sender-id :message {:text "Premièrement j'ai accès à quelques informations vous concernant !"})
    (post-messenger sender-id :message {:text "Premièrement j'ai accès à quelques informations vous concernant !"})))



(defn routing [entry]
  (let [sender-id (keyword (str (get-in entry [:sender :id])))]
    (post-messenger sender-id :message {:text "Explorez les différentes sections :"})
    (post-messenger sender-id :message {:attachment {:type    "template"
                                                     :payload (send-api/make-generic-template
                                                                (send-api/make-element-generic-object
                                                                  {:title    "Template de type bouton"
                                                                   :subtitle "Les boutons sont les call-to-action de messenger."
                                                                   :buttons  [(send-api/make-postback-button
                                                                                {:title   "Button Template"
                                                                                 :payload "button-template"})]})
                                                                (send-api/make-element-generic-object
                                                                  {:title    "Template de type générique"
                                                                   :subtitle "Ils permettent de présenter l'information."
                                                                   :buttons  [(send-api/make-postback-button
                                                                                {:title   "Generic Template"
                                                                                 :payload "generic-template"})]})
                                                                (send-api/make-element-generic-object
                                                                  {:title    "Template de type list"
                                                                   :subtitle "Ils permettent de présenter l'information sous forme d'une liste."
                                                                   :buttons  [(send-api/make-postback-button
                                                                                {:title   "List Template"
                                                                                 :payload "list-template"})]}))}})))


(def intro-route
  (routes (messenger-route
            :start
            :message
            (fn [entry]
              :routing)
            nil)
          (messenger-route
            :routing
            :read
            (fn [entry]
              :my-name)
            intro-action)
          (messenger-route
            ::my-name
            :read
            (fn [entry]
              :default-question)
            my-name-action)
          (messenger-route
            :default-question
            :postback
            (fn [entry]
              (if (contains? entry :read)
                :routing)
              (let [payload (get-in entry [:postback :payload])]
                (condp = payload
                      "button-template" :button-template-start
                      "generic-template" :generic-template-start
                      "list-template" :list-template-start
                      :default :end)))
            routing)))
