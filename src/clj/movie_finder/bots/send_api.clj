(ns movie-finder.bots.send-api
  (:require [movie-finder.bots.spec :as spec]
            [clojure.spec :as s]))

;; Pour chaque button
;; Faire une function pour créer le button
;; Validation
;; Specing functions
;; Les tester dans le showcase

;;;;;;;;;;;;;;;;;;;;;;; Button Template ;;;;;;;;;;;;;;;;;;;
;;See:
;; https://developers.facebook.com/docs/messenger-platform/send-api-reference/button-template
;; https://developers.facebook.com/docs/messenger-platform/send-api-reference/message-buttons

{:attachment
 {:type "template"
  :payload
  {:template_type "button"
   :text "This should be text predefined by the user"
   :buttons
   [{:type "postback"
     :title "Predefined by user"
     :payload "DEVELOPER_DEFINED_PAYLOAD"}]}}}

(defn make-button-template [text & buttons]
  (let [button-template
        {:template_type "button"
         :text          text
         :buttons       (vec buttons)}
        parsed (s/explain-data :button-template/button_template button-template)]
    (if (= parsed ::s/invalid)
      (throw (ex-info "Invalid input" (s/explain-data :button-template/button_template button-template)))
      button-template)))

(defn make-url-button [] ,,)
(defn make-postback-button [{:keys [title payload]}]
  (let [button {:type    "postback"
                :title   title
                :payload payload}
        parsed (s/conform :button/button button)]
    (if (= parsed ::s/invalid)
      (throw (ex-info "Invalid input" (s/explain-data :button/button button)))
      button)))
(defn make-call-button [] ,,)
(defn make-share-button [] ,,)
(defn make-buy-button [] ,,)
(defn make-log-in-button [] ,,)
(defn make-log-out-button [] ,,)
