(ns movie-finder.bots.core)

;; ========================== PROTOCOL DEFINITION =============================
(defprotocol ElementMessenger
  ;https://developers.facebook.com/docs/messenger-platform/send-api-reference/list-template
  "This protocol is tied to element from messenger list"
  (make-element [this]))

(defprotocol MessageButton
  ;;https://developers.facebook.com/docs/messenger-platform/send-api-reference/message-buttons#fields
  "Protocol "
  (make-url-button [this title url])
  (make-postback-button [this])
  (make-call-button [this])
  (make-share-button [this]))

;; ========================== PROTOCOL IMPLEMENTATION =========================

(defn make-list-template [movies]
  {:type "template"
   :payload
         {:template_type "list"
          :elements      (vec (take 4 (map #(make-element %) movies)))}})

;; ========================== PROTOCOL DEFINITION AND IMPLEMENTATION ==========

(defrecord Movie [poster_path
                  video
                  popularity
                  release_date
                  vote_average
                  overview
                  original_language
                  title
                  original_title
                  vote_count
                  adult
                  backdrop_path
                  id
                  genre_ids])

(extend-type Movie
  ElementMessenger
  (make-element [this]
    {:title (get this :title)
     :subtitle (get this :overview)
     :image_url (get this :poster_path)
     :default_action (make-url-button this nil "http://google.com")
     :buttons [(make-url-button this "Voir le Film" "http://google.com")]})
  MessageButton
  (make-url-button [_ title url]
    {:type "web_url"
     :title title
     :url url
     :webview_height_ratio "compact"
     :messenger_extension false
     :fallback_url url}))
