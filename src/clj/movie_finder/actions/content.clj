(ns movie-finder.actions.content
  (:require [movie-finder.actions.core :refer [messenger-route routes]]
            [movie-finder.bots.network :refer [post-messenger typing-on]]
            [movie-finder.bots.send-api :as send-api]))

(defn content-introduction-action [entry]
  (let [sender-id (keyword (str (get-in entry [:sender :id])))]
    (typing-on sender-id)
    (Thread/sleep 5000)
    (post-messenger sender-id :message {:text "Parlons maintenant du type de contenu que je peux vous envoyer !"})
    (typing-on sender-id)
    (Thread/sleep 2000)
    (post-messenger sender-id :message {:text "J'ai les mêmes droits qu'un humain et à ce titre je peux vous envoyer du texte (comme maintenant) des fichiers audios, vidéos, des images et des fichiers classiques."})))

(defn content-image-action [entry]
  (let [sender-id (keyword (str (get-in entry [:sender :id])))]
    (typing-on sender-id)
    (Thread/sleep 5000)
    (post-messenger sender-id :message {:text "Voici une image classique :"})
    (typing-on sender-id)
    (Thread/sleep 2000)
    (post-messenger sender-id :message {:attachment {:type "image"
                                                     :payload {:url "http://www.frenchweb.fr/wp-content/uploads/2013/06/Accelerateur1.png"}}})))

(defn content-audio-action [entry]
  (let [sender-id (keyword (str (get-in entry [:sender :id])))]
    (typing-on sender-id)
    (Thread/sleep 5000)
    (post-messenger sender-id :message {:text "Voici un fichier mp3 :"})
    (post-messenger sender-id :message {:attachment {:type "audio"
                                                     :payload {:url "https://ia800500.us.archive.org/5/items/aesop_fables_volume_one_librivox/fables_01_17_aesop.mp3"}}})))

(defn content-video-action [entry]
  (let [sender-id (keyword (str (get-in entry [:sender :id])))]
    (typing-on sender-id)
    (Thread/sleep 5000)
    (post-messenger sender-id :message {:text "Voici un fichier vidéo : (coming after uploading)"})
    (comment (post-messenger sender-id :message {:attachment {:type    "video"
                                                              :payload {:url "https://youtu.be/51CHPYYKUoE"}}}))))


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
              :content-vidéo)
            content-audio-action)
          (messenger-route
            :content-vidéo
            #{:message :postback}
            (fn [entry]
              :routing)
            content-video-action)))

;;https://ia800500.us.archive.org/5/items/aesop_fables_volume_one_librivox/fables_01_17_aesop.mp3
;;https://youtu.be/51CHPYYKUoE
