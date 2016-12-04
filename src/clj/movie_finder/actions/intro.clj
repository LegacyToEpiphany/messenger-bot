(ns movie-finder.actions.intro
  (:require [movie-finder.actions.core :refer [messenger-route routes]]
            [movie-finder.bots.network :refer [post-messenger typing-on]]
            [movie-finder.bots.send-api :as send-api]))

(defn intro-action [entry]
  (let [sender-id (keyword (str (get-in entry [:sender :id])))]
    (post-messenger sender-id :message {:text "Bonjour et bienvenue dans ce bot Showcase !"})
    (typing-on sender-id)
    (Thread/sleep 1000)
    (post-messenger sender-id :message {:text "Ce bot a pour unique vocation à vous présenter l'ensemble des possibilités offertes par la plateforme Messenger"})
    (typing-on sender-id)
    (Thread/sleep 1000)
    (post-messenger sender-id :message {:text "Je vous propose de choisir le type de d'actions que vous souhaitez explorer:"})
    (typing-on sender-id)
    (Thread/sleep 2000)
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
                                                                                 :payload "payload"})]})
                                                                (send-api/make-element-generic-object
                                                                  {:title    "Template de type list"
                                                                   :subtitle "Ils permettent de présenter l'information sous forme d'une liste."
                                                                   :buttons  [(send-api/make-postback-button
                                                                                {:title   "List Template"
                                                                                 :payload "payload"})]}))}})))


(def intro-route
  (routes (messenger-route
            :start
            (fn [entry]
              :routing)
            intro-action)
          (messenger-route
            :routing
            (fn [entry]
              (println entry)
              (if (= (get-in entry [:postback :payload]) "button-template")
                :button-template-start
                :end))
            (fn [entry]))))
