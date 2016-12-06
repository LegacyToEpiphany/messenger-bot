(ns movie-finder.actions.intro
  (:require [movie-finder.actions.core :refer [messenger-route routes]]
            [movie-finder.bots.network :refer [post-messenger typing-on]]
            [movie-finder.bots.send-api :as send-api]
            [movie-finder.bots.settings :refer [get-user-profile]]
            [cheshire.core :refer [parse-string]])
  (:import [java.util Date]))

(defn intro-action [entry]
  (let [sender-id (keyword (str (get-in entry [:sender :id])))]
    (post-messenger sender-id :message {:text "Bonjour et bienvenue dans ce bot de Showcase !"})
    (typing-on sender-id)))

(defn my-name-action [entry]
  (let [sender-id (keyword (str (get-in entry [:sender :id])))]
    (typing-on sender-id)
    (post-messenger sender-id :message {:text "Je m'appelle Lambda et je vais vous montrez l'ensemble des possibilités offertes par la plateforme Messenger !"})))

(defn your-info-explanation-action [entry]
  (let [sender-id (keyword (str (get-in entry [:sender :id])))]
    (typing-on sender-id)
    (Thread/sleep 2000)
    (post-messenger sender-id :message {:text "Premièrement j'ai accès à quelques informations vous concernant !"})
    (typing-on sender-id)
    (Thread/sleep 1000)
    (post-messenger sender-id :message {:text "Pour être exacte je connais votre nom, prénom, votre langue et votre timezone"})))

(defn your-info-action [entry]
  (let [sender-id (keyword (str (get-in entry [:sender :id])))
        profile (parse-string (:body (get-user-profile (get-in entry [:sender :id]))) true)]
    (typing-on sender-id)
    (Thread/sleep 2000)
    (post-messenger sender-id :message {:text (str "Je sais que vous vous appelez "
                                                   (:first_name profile) " " (:last_name profile)
                                                   (if (= "male" (:gender profile))
                                                     " et que vous êtes un homme français."
                                                     " et que vous êtes une femme française."))})
    (post-messenger sender-id :message {:text "J'ai même accès à votre photo de profil facebook \uD83D\uDE00 \uD83D\uDCAA \uD83D\uDCAA \uD83D\uDCAA"})
    (typing-on sender-id)
    (Thread/sleep 2000)
    (post-messenger sender-id :message {:attachment {:type "image"
                                                     :payload {:url (:profile_pic profile)}}})))

(defn delivery-info-action [entry]
  (let [sender-id (keyword (str (get-in entry [:sender :id])))
        delivery-timestamp (get-in entry [:delivery :watermark])]
    (typing-on sender-id)
    (post-messenger sender-id :message {:text "Pour information je sais également quand votre téléphone reçoit mes messages et quand vous les lisez \uD83D\uDE0E \uD83D\uDE0E \uD83D\uDE0E !!!"})
    (post-messenger sender-id :message {:text (str "Votre téléphone a reçu le dernier message à "
                                                   (.getHours (Date. (long delivery-timestamp))) "h"
                                                   (.getMinutes (Date. (long delivery-timestamp))) " et "
                                                   (.getSeconds (Date. (long delivery-timestamp))) "s")})))

(defn read-info-action [entry]
  (let [sender-id (keyword (str (get-in entry [:sender :id])))
        read-timestamp (get-in entry [:read :watermark])]
    (typing-on sender-id)
    (Thread/sleep 3000)
    (post-messenger sender-id :message {:text (str "De la même façon je sais que vous avez vu le dernier message à "
                                                   (.getHours (Date. (long read-timestamp))) "h"
                                                   (.getMinutes (Date. (long read-timestamp))) " et "
                                                   (.getSeconds (Date. (long read-timestamp))) "s")})))



(def intro-route
  (routes (messenger-route
            :start
            #{:message :postback :read}
            (fn [entry]
              :routing)
            nil)
          (messenger-route
            :routing
            #{:read}
            (fn [entry]
              :my-name)
            intro-action)
          (messenger-route
            :my-name
            #{:read}
            (fn [entry]
              :your-info-description)
            my-name-action)
          (messenger-route
            :your-info-description
            #{:read}
            (fn [entry]
              :your-info-action)
            your-info-explanation-action)
          (messenger-route
            :your-info-action
            #{:delivery}
            (fn [entry]
              :delivery-info-action)
            your-info-action)
          (messenger-route
            :delivery-info-action
            #{:read}
            (fn [entry]
              :read-info-action)
            delivery-info-action)
          (messenger-route
            :read-info-action
            #{:read}
            (fn [entry]
              :content-intro)
            read-info-action)))
