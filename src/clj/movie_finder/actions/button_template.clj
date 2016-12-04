(ns movie-finder.actions.button-template
  (:require [movie-finder.actions.core :refer [messenger-route routes]]
            [movie-finder.bots.network :refer [post-messenger typing-on]]
            [movie-finder.bots.send-api :as send-api]))

(defn button-intro-action [entry]
  (let [sender-id (keyword (str (get-in entry [:sender :id])))]
    (post-messenger sender-id :message {:text "Les boutons sont les call-to-action de la plateforme messenger"})
    (typing-on sender-id)
    (Thread/sleep 1000)
    (post-messenger sender-id :message {:text "On distingue 7 types de boutons et nous allons tous les voir"})
    (typing-on sender-id)
    (Thread/sleep 1000)
    (post-messenger sender-id :message {:text "1) URL Button"})
    (typing-on sender-id)
    (Thread/sleep 1000)
    (post-messenger sender-id :message {:text "Ils permettent de rediriger l'utilisateur vers une page web."})
    (typing-on sender-id)
    (Thread/sleep 1000)
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

(defn button-call-action [entry]
  (let [sender-id (keyword (str (get-in entry [:sender :id])))]
    (post-messenger sender-id :message {:text "Le bouton d'appel permet de composer un numéro de téléphone depuis Messenger."})
    (Thread/sleep 1000)
    (post-messenger sender-id :message {:attachment {:type    "template"
                                                     :payload (send-api/make-button-template
                                                                "Appeler notre call-center"
                                                                (send-api/make-call-button
                                                                  {:title   "Nous joindre"
                                                                   :payload "+33630867395"})
                                                                (send-api/make-postback-button
                                                                  {:title   "Continuer"
                                                                   :payload "share-button"}))}})))

(defn button-share-action [entry]
  (let [sender-id (keyword (str (get-in entry [:sender :id])))]
    (post-messenger sender-id :message {:text "Le bouton share permet de partager du contenu avec d'autres utilisateurs de Messenger."})
    (Thread/sleep 1000)
    (post-messenger sender-id :message {:text "Il a l'avantage de permettre à un utilisateur de faire découvrir le bot à son entourage."})
    (Thread/sleep 2000)
    (post-messenger sender-id :message {:attachment {:type    "template"
                                                     :payload (send-api/make-generic-template
                                                                (send-api/make-element-generic-object
                                                                  {:title "L'accélérateur devient Day One"
                                                                   :item_url "http://www.dayonepartners.com/fr/"
                                                                   :image_url "http://www.dayonepartners.com/fr/wp-content/uploads/sites/3/2016/11/logo-2.png"
                                                                   :subtitle "C'est une grande nouvelle ! :-)"
                                                                   :buttons [(send-api/make-share-button)]}))}})))

(def button-template-route
  (routes (messenger-route :button-template-start
                           (fn [entry]
                             (if (= "call-button" (get-in entry [:postback :payload]))
                               :button-template-call))
                           button-intro-action)
          (messenger-route :button-template-call
                           (fn [entry]
                             (if (= "share-button" (get-in entry [:postback :payload]))
                               :button-template-share)) button-call-action)
          (messenger-route :button-template-share (fn [entry] :end) button-share-action)))
