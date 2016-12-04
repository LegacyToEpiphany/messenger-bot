(ns movie-finder.actions.generic-template
  (:require [movie-finder.actions.core :refer [messenger-route routes]]
            [movie-finder.bots.network :refer [post-messenger typing-on]]
            [movie-finder.bots.send-api :as send-api]))

(defn generic-intro-action [entry]
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

(def generic-template-route
  (routes (messenger-route :generic-template-start
                           (fn [entry]
                             (if (= "call-button" (get-in entry [:postback :payload]))
                               :button-template-call))
                           generic-intro-action)))
