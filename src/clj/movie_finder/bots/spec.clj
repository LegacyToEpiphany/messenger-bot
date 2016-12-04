(ns movie-finder.bots.spec
  (:require [clojure.spec :as s]
            [clojure.spec.gen :as gen]))

;; ========================== REGISTRY FOR BUTTON =============================
(def phone-regex #"^[+][1-9]+")
(s/def ::phone-type (s/and string? #(re-matches phone-regex %)))
;TODO: implement the right regex for international phone numbers
(s/def :call-button/payload #{"+33630867395"})

;TODO: implement the right regex for urls
(s/def ::url string?)
(s/def :button/type string?)
(s/def :button/title (s/and string? #(<= (count %) 20)))
(s/def :button/payload (s/and string? #(<= (count %) 1000)))

(s/def :button/url ::url)
(s/def :button/webview_height_ratio #{"compact" "tall" "full"})
(s/def :button/messenger_extensions boolean?)
(s/def :button/fallback_url ::url)

(defmulti button-type :type)
;; Postback button
;; https://developers.facebook.com/docs/messenger-platform/send-api-reference/postback-button
(defmethod button-type "postback" [_]
  (s/keys :req-un [:button/title :button/type :button/payload]))
;; Call button
;; https://developers.facebook.com/docs/messenger-platform/send-api-reference/call-button
(defmethod button-type "phone_number" [_]
  (s/keys :req-un [:button/title :button/type :call-button/payload]))
;; URL button
;; https://developers.facebook.com/docs/messenger-platform/send-api-reference/url-button
;; Don't forget to "whitelist" your domain for url button and/or messenger extensions
(defmethod button-type "web_url" [_]
  (s/keys :req-un [:button/title :button/type :button/url]
          :opt-un [:button/webview_height_ratio
                   :button/messenger_extensions
                   :button/fallback_url]))

(defmethod button-type "element_share" [_]
  (s/keys :req-un [:button/type]))

;TODO: Add buy button and log-in/log-out button
;TODO: the share button only work with the generic template, not specified here.
;;NOTE: https://developers.facebook.com/docs/messenger-platform/send-api-reference/share-button
;(defmethod button-type "element_share" [_]
;  (s/keys :req-un [:button/type]))

(s/def :button/button (s/multi-spec button-type :type))

;; ========================== ELEMENT OBJECT =================================

;TODO: spec the fact that one element cannot have both default_action and item_url
;TODO: Spec this error : incomplete element
; title and at least one other field (image url, subtitle or buttons) is required
(s/def :element/title (s/and string? #(<= (count %) 80)))
(s/def :element/item_url ::url)
(s/def :element/image_url ::url)
(s/def :element/subtitle (s/and string? #(<= (count %) 80)))
(s/def :generic-template/buttons (s/coll-of :button/button
                                   :kind vector?
                                   :max-count 3
                                   :into []))
(s/def :list-template/buttons (s/coll-of :button/button
                                            :kind vector?
                                            :max-count 1
                                            :into []))

;TODO: Spec that :button/type should only be a web_url type in a default_action
(s/def :element/default_action
  (s/keys :req-un [:button/type :button/url]
          :opt-un [:button/webview_height_ratio
                   :button/messenger_extensions
                   :button/fallback_url]))

(s/def :generic-template/element
        (s/keys :req-un [:element/title]
                :opt-un [:element/item_url
                         :element/image_url
                         :element/subtitle
                         :generic-template/buttons
                         :element/default_action]))

(s/def :list-template/element
  (s/keys :req-un [:element/title]
          :opt-un [:element/item_url
                   :element/image_url
                   :element/subtitle
                   :list-template/buttons
                   :element/default_action]))

;; ========================== PAYLOAD BUTTON TEMPLATE =========================
(s/def :button-template/template_type #{"button"})
;TODO: specify that encoding is set to UTF-8
(s/def :button-template/text (s/and string? #(<= (count %) 320)))
(s/def :button-template/buttons
  (s/coll-of :button/button
             :kind vector?
             :min-count 1
             :max-count 3
             :into []))

(s/def :button-template/button_template
  (s/keys :req-un
          [:button-template/template_type
           :button-template/text
           :button-template/buttons]))

;; ========================== PAYLOAD GENERIC TEMPLATE ========================
(s/def :generic-template/template_type #{"generic"})
(s/def :generic-template/elements
  (s/coll-of :generic-template/element
             :kind vector?
             :min-count 1
             :max-count 10
             :into []))

(s/def :generic-template/generic_template
  (s/keys :req-un
          [:generic-template/template_type
           :generic-template/elements]))

;; ========================== PAYLOAD LIST TEMPLATE ========================
(s/def :list-template/template_type #{"list"})
(s/def :list-template/top_element_style #{"large" "compact"})
(s/def :list-template/elements
  (s/coll-of :list-template/element
             :kind vector?
             :min-count 2
             :max-count 4
             :into []))
(s/def :list-template/buttons
  (s/coll-of :button/button
             :kind vector?
             :min-count 1
             :max-count 1
             :into []))

(s/def :list-template/list-template
  (s/keys :req-un
          [:list-template/template_type
           :list-template/elements]
          :opt-un
          [:list-template/top_element_style
           :list-template/buttons]))

;; ========================== REGISTRY FOR ATTACHMENT OF TEMPLATE =============
(s/def :attachment/type #{"template"})
(s/def :attachment/payload :button-template/button_template)

(s/def :attachment/attachment
  (s/keys :req-un
          [:attachment/type
           :attachment/payload]))
