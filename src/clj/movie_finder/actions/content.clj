(ns movie-finder.actions.content
  (:require [movie-finder.actions.core :refer [messenger-route routes]]
            [movie-finder.bots.network :refer [post-messenger typing-on]]
            [movie-finder.bots.send-api :as send-api]))

(defn content-introduction-action [entry]
  (let [sender-id (keyword (str (get-in entry [:sender :id])))]
    (typing-on sender-id)
    (post-messenger sender-id :message {:text "Parlons maintenant du type de contenu que je peux vous envoyer !"})
    (typing-on sender-id)
    (post-messenger sender-id :message {:text "J'ai les mêmes droits qu'un humain et à ce titre je peux vous envoyer du texte (comme maintenant) des fichiers audios, vidéos, des images et des fichiers classiques."})))

(defn content-image-action [entry]
  (let [sender-id (keyword (str (get-in entry [:sender :id])))]
    (post-messenger sender-id :message {:text "Voici une image classique :"})
    (post-messenger sender-id :message {:attachment {:type    "image"
                                                     :payload {:url "http://www.frenchweb.fr/wp-content/uploads/2013/06/Accelerateur1.png"}}})))

(defn content-audio-action [entry]
  (let [sender-id (keyword (str (get-in entry [:sender :id])))]
    (post-messenger sender-id :message {:text "Voici un fichier mp3 :"})
    (post-messenger sender-id :message {:attachment {:type    "audio"
                                                     :payload {:url "https://ia800500.us.archive.org/5/items/aesop_fables_volume_one_librivox/fables_01_17_aesop.mp3"}}})))

(defn content-video-action [entry]
  (let [sender-id (keyword (str (get-in entry [:sender :id])))]
    (post-messenger sender-id :message {:text "Voici un fichier vidéo : (coming after uploading)"})
    (comment (post-messenger sender-id :message {:attachment {:type    "video"
                                                              :payload {:url "https://video.xx.fbcdn.net/v/t42.3356-2/15398718_1254214061316970_465507964497690624_n.mp4/video-1481016101.mp4?vabr=422381&oh=eb83024ebd63e211dca2335248aa3b8a&oe=584801BF"}}}))))

(defn button-template-routing-action [entry]
  (let [sender-id (keyword (str (get-in entry [:sender :id])))]
    (typing-on sender-id)
    (post-messenger sender-id :message {:text "Je vais vous présenter maintenant les différentes façons de pousser de l'information à l'utilisateur !"})
    (post-messenger sender-id :message {:attachment {:type    "template"
                                                     :payload (send-api/make-button-template
                                                                "Appuyer sur le bouton pour continuer !"
                                                                (send-api/make-postback-button
                                                                  {:title   "Continuer"
                                                                   :payload "button-template-intro"}))}})))


(def content-route
  (routes (messenger-route
            :content-intro
            #{:read}
            (fn [entry]
              :content-image)
            content-introduction-action)
          (messenger-route
            :content-image
            #{:read}
            (fn [entry]
              :content-audio)
            content-image-action)
          (messenger-route
            :content-audio
            #{:read}
            (fn [entry]
              :content-video)
            content-audio-action)
          (messenger-route
            :content-video
            #{:read}
            (fn [entry]
              :button-template-routing)
            content-video-action)
          (messenger-route
            :button-template-routing
            #{:postback}
            (fn [entry]
              (println "postback")
              (println entry)
              (if (= "button-template-intro" (get-in entry [:postback :payload]))
                :button-intro
                :button-template-routing))
            button-template-routing-action)))