(ns movie-finder.actions.button-template
  (:require [movie-finder.actions.core :refer [messenger-route routes]]
            [movie-finder.bots.network :refer [post-messenger typing-on]]
            [movie-finder.bots.send-api :as send-api]))

(defn button-intro-action [entry]
  (let [sender-id (keyword (str (get-in entry [:sender :id])))]
    (post-messenger sender-id :message {:text "Les boutons sont les call-to-action de la plateforme messenger"})))

(defn button-description-action [entry]
  (let [sender-id (keyword (str (get-in entry [:sender :id])))]
    (post-messenger sender-id :message {:text "Ils permettent d'ouvrir une page web, de répondre à une question, d'appeler un numéro ou de partager du contenu."})))

(defn button-url-action [entry]
  (let [sender-id (keyword (str (get-in entry [:sender :id])))]
    (post-messenger sender-id :message {:text "Par exemple le button de type \"URL\" permet d'ouvrir une page web :"})
    (post-messenger sender-id :message {:attachment {:type    "template"
                                                     :payload (send-api/make-button-template
                                                                "Appuyer sur le boutton pour ouvrir l'URL"
                                                                (send-api/make-url-button
                                                                  {:url                  "http://www.dayonepartners.com/fr/"
                                                                   :title                "Day One - Compact"
                                                                   :webview_height_ratio "compact"})
                                                                (send-api/make-url-button
                                                                  {:url                  "http://www.dayonepartners.com/fr/"
                                                                   :title                "Day One - Tall"
                                                                   :webview_height_ratio "tall"})
                                                                (send-api/make-postback-button
                                                                  {:title   "Continuer"
                                                                   :payload "call-button"}))}})))

(defn button-call-description-action [entry]
  (let [sender-id (keyword (str (get-in entry [:sender :id])))]
    (post-messenger sender-id :message {:text "Un autre bouton très utile est le bouton permettant d'appeler un numéro !"})))

(defn button-call-description-step-two-action [entry]
  (let [sender-id (keyword (str (get-in entry [:sender :id])))]
    (post-messenger sender-id :message {:text "On imagine un cas d'usage très utile dans le cadre d'un bot e-commerce ! "})))

(defn button-call-action [entry]
  (let [sender-id (keyword (str (get-in entry [:sender :id])))]
    (post-messenger sender-id :message {:attachment {:type    "template"
                                                     :payload (send-api/make-button-template
                                                                "Appeler notre call-center"
                                                                (send-api/make-call-button
                                                                  {:title   "Nous joindre"
                                                                   :payload "+33630867395"})
                                                                (send-api/make-postback-button
                                                                  {:title   "Continuer"
                                                                   :payload "share-button"}))}})))

(defn button-share-description-step-one-action [entry]
  (let [sender-id (keyword (str (get-in entry [:sender :id])))]
    (post-messenger sender-id :message {:text "Il est également possible de créer un bouton permettant le partage de l'information!"})))

(defn button-share-description-step-two-action [entry]
  (let [sender-id (keyword (str (get-in entry [:sender :id])))]
    (post-messenger sender-id :message {:text "Il a l'avantage de permettre d'initier une conversation avec un nouvel utilisateur lors du partage."})))

(defn button-share-action [entry]
  (let [sender-id (keyword (str (get-in entry [:sender :id])))]
    (post-messenger sender-id :message {:attachment {:type    "template"
                                                     :payload (send-api/make-generic-template
                                                                (send-api/make-element-generic-object
                                                                  {:title "L'accélérateur devient Day One"
                                                                   :item_url "http://www.dayonepartners.com/fr/"
                                                                   :image_url "http://www.dayonepartners.com/fr/wp-content/uploads/sites/3/2016/11/logo-2.png"
                                                                   :subtitle "C'est une grande nouvelle ! :-)"
                                                                   :buttons [(send-api/make-share-button)
                                                                             (send-api/make-postback-button
                                                                               {:title   "Continuer"
                                                                                :payload "generic-template"})]}))}})))

(def button-template-route
  (routes (messenger-route
            :button-intro
            #{:read}
            (fn [entry]
              :button-description)
            button-intro-action)
          (messenger-route
            :button-description
            #{:read}
            (fn [entry]
              :button-url-action)
            button-description-action)
          (messenger-route
            :button-url-action
            #{:postback}
            (fn [entry]
              (if (= "call-button" (get-in entry [:postback :payload]))
                :button-call-description-action
                :button-url-action))
            button-url-action)
          (messenger-route
            :button-call-description-action
            #{:read}
            (fn [entry]
              :button-call-description-step-two-action)
            button-call-description-action)
          (messenger-route
            :button-call-description-step-two-action
            #{:read}
            (fn [entry]
              :button-call-action)
            button-call-description-step-two-action)
          (messenger-route
            :button-call-action
            #{:postback}
            (fn [entry]
              (if (= "share-button" (get-in entry [:postback :payload]))
                :button-share-description-step-one-action
                :button-call-action))
            button-call-action)
          (messenger-route
            :button-share-description-step-one-action
            #{:read}
            (fn [entry]
              :button-share-description-step-two-action)
            button-share-description-step-one-action)
          (messenger-route
            :button-share-description-step-two-action
            #{:read}
            (fn [entry]
              :button-share-action)
            button-share-description-step-two-action)
          (messenger-route
            :button-share-action
            #{:postback}
            (fn [entry]
              (if (= "generic-template" (get-in entry [:postback :payload]))
                :generic-template-introduction-action
                :button-share-action))
            button-share-action)))