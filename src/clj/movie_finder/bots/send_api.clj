(ns movie-finder.bots.send-api
  (:require [movie-finder.bots.spec :as spec]
            [clojure.spec :as s]))

;; TODO Spec functions
;; TODO Integrate Messenger Extension
;; TODO Make Share button for generic template
;; TODO Make the URL Button with URL Extension a true standard
;; TODO Make buy button
;; TODO Make Log-in button
;; TODO Make Log-out button

;;;;;;;;;;;;;;;;;;;;;;; Buttons ;;;;;;;;;;;;;;;;;;;
;;See:
;; https://developers.facebook.com/docs/messenger-platform/send-api-reference/buttons

(defn make-call-button [{:keys [title payload]}]
  (let [button {:type    "phone_number"
                :title   title
                :payload payload}
        parsed (s/conform :button/button button)]
    (if (= parsed ::s/invalid)
      (throw (ex-info "Invalid input" (s/explain-data :button/button button)))
      button)))

(defn make-postback-button [{:keys [title payload]}]
  (let [button {:type    "postback"
                :title   title
                :payload payload}
        parsed (s/conform :button/button button)]
    (if (= parsed ::s/invalid)
      (throw (ex-info "Invalid input" (s/explain-data :button/button button)))
      button)))

(defn make-url-button [{:keys [url title webview_height_ratio]}]
  (let [button {:type    "web_url"
                :title   title
                :url url
                :webview_height_ratio webview_height_ratio}
        parsed (s/conform :button/button button)]
    (if (= parsed ::s/invalid)
      (throw (ex-info "Invalid input" (s/explain-data :button/button button)))
      button)))

(defn make-share-button [] ,,)
(defn make-buy-button [] ,,)
(defn make-log-in-button [] ,,)
(defn make-log-out-button [] ,,)



;;;;;;;;;;;;;;;;;;;;;;; Button Template ;;;;;;;;;;;;;;;;;;;
;;See:
;; https://developers.facebook.com/docs/messenger-platform/send-api-reference/button-template

(defn make-button-template [text & buttons]
  (let [button-template
        {:template_type "button"
         :text          text
         :buttons       (vec buttons)}
        parsed (s/explain-data :button-template/button_template button-template)]
    (if (= parsed ::s/invalid)
      (throw (ex-info "Invalid input" (s/explain-data :button-template/button_template button-template)))
      button-template)))



;;;;;;;;;;;;;;;;;;;;;;; Generic Template ;;;;;;;;;;;;;;;;;;;
;;See:
;; https://developers.facebook.com/docs/messenger-platform/send-api-reference/generic-template
