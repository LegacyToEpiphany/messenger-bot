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

;; ========================== PAYLOAD AIRLINE BOARDING PASS TEMPLATE ==========
;;https://developers.facebook.com/docs/messenger-platform/send-api-reference/airline-boardingpass-template

;; FIELD OBJECT

(s/def :field/label string?)
(s/def :field/value string?)

(s/def :field/field (s/keys :req-un
                            [:field/label
                             :field/value]))

;; FLIGHT SCHEDULE OBJECT
(def ISO-8601-based-regex #"^(\d{4})-(\d{2})-(\d{2})T(\d{2})\:(\d{2})$")
(s/def :flight_schedule/boarding_time (s/and string? #(re-matches ISO-8601-based-regex %)))
(s/def :flight_schedule/departure_time (s/and string? #(re-matches ISO-8601-based-regex %)))
(s/def :flight_schedule/arrival_time (s/and string? #(re-matches ISO-8601-based-regex %)))

(s/def :flight_schedule/:flight_schedule (s/keys :req-un
                                                 [:flight_schedule/departure_time]
                                                 :opt-un
                                                 [:flight_schedule/boarding_time
                                                  :flight_schedule/arrival_time]))

;; AIRPORT OBJECT
(s/def :airport/airport_code string?)
(s/def :airport/city string?)
(s/def :airport/terminal string?)
(s/def :airport/gate string?)

(s/def :airport/airport (s/keys :req-un
                                [:airport/airport_code
                                 :airport/city]
                                :opt-un
                                [:airport/terminal
                                 :airport/gate]))


;; FLIGHT INFO OBJECT
(s/def :flight_info/flight_number string?)
(s/def :flight_info/departure_airport :airport/airport)
(s/def :flight_info/arrival_airport :airport/airport)
(s/def :flight_info/flight_schedule :flight_schedule/:flight_schedule)

(s/def :flight_info/flight_info (s/keys :req-un
                                        [:flight_info/flight_number
                                         :flight_info/departure_airport
                                         :flight_info/arrival_airport
                                         :flight_info/flight_schedule]))

;; BOARDING PASS OBJECT
(s/def :boarding_pass/passenger_name string?)
(s/def :boarding_pass/pnr_number string?)
(s/def :boarding_pass/travel_class #{"economy" "business" "first_class"})
(s/def :boarding_pass/seat string?)
(s/def :boarding_pass/auxiliary_fields (s/coll-of :field/field
                                                  :kind vector?
                                                  :max-count 5
                                                  :into []))
(s/def :boarding_pass/secondary_fields (s/coll-of :field/field
                                                  :kind vector?
                                                  :max-count 5
                                                  :into []))
(s/def :boarding_pass/logo_image_url ::url)
(s/def :boarding_pass/header_image_url ::url)
(s/def :boarding_pass/header_text_field :field/field)
(s/def :boarding_pass/qr_code string?)
(s/def :boarding_pass/barcode_image_url ::url)
(s/def :boarding_pass/above_bar_code_image_url ::url)
(s/def :boarding_pass/flight_info :flight_info/flight_info)

(s/def :boarding_pass/boarding_pass
  (s/keys :req-un
          [:boarding_pass/passenger_name
           :boarding_pass/pnr_number
           :boarding_pass/logo_image_url
           :boarding_pass/above_bar_code_image_url
           :boarding_pass/flight_info]
          :opt-un
          [:boarding_pass/travel_class
           :boarding_pass/seat
           :boarding_pass/auxiliary_fields
           :boarding_pass/secondary_fields
           :boarding_pass/header_image_url
           :boarding_pass/header_text_field
           :boarding_pass/qr_code
           :boarding_pass/barcode_image_url]))

;; AIRLINE BOARDING PASS PAYLOAD
(s/def :airline_boardingpass/template_type #{"airline_boardingpass"})
(s/def :airline_boardingpass/intro_message ::url)
(s/def :airline_boardingpass/locale ::url)
(s/def :airline_boardingpass/theme_color ::url)
(s/def :airline_boardingpass/boarding_pass ::url)


;; ========================== REGISTRY FOR ATTACHMENT OF TEMPLATE =============
(s/def :attachment/type #{"template"})
(s/def :attachment/payload :button-template/button_template)

(s/def :attachment/attachment
  (s/keys :req-un
          [:attachment/type
           :attachment/payload]))
