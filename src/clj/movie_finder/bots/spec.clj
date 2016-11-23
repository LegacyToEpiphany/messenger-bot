(ns movie-finder.bots.spec
  (:require [clojure.spec :as s]
            [clojure.spec.gen :as gen]))

;; ========================== SPEC HELPERS ====================================
(defn ns-as
  "Creates a namespace 'n' (if non existant) and then aliases it to 'a'"
  [n a]
  (create-ns n)
  (alias a n))

;; ========================== REGISTRY FOR BUTTON =============================
(s/def :button/type string?)
(s/def :button/title (s/and string? #(<= (count %) 20)))
(s/def :button/payload (s/and string? #(<= (count %) 1000)))

(def phone-regex #"^[+][1-9]+")
(s/def ::phone-type (s/and string? #(re-matches phone-regex %)))
;;regex to implement (internaltional phone number)
(s/def :test/payload #{"+16505551234"})

(defmulti button-type :button/type)
(defmethod button-type "postback" [_]
  (s/keys :req
          [:button/type
           :button/title
           :button/payload]))
(defmethod button-type "phone_number" [_]
  (s/keys :req
          [:button/type
           :button/title
           :test/payload]))

(s/def :button/button (s/multi-spec button-type :button/type))

;; ========================== REGISTRY FOR PAYLOAD TEMPLATE ===================
(ns-as 'messenger.payload
       'payload-options.button)
(s/def ::payload-options.button/template_type #{"button"})
(s/def ::payload-options.button/text (s/and string? #(<= (count %) 320)))          ;;TODO: check encoding set to UTF-8
(s/def ::payload-options.button/buttons
  (s/coll-of ::button-options.button/postback-button
             :kind vector?
             :min-count 1
             :max-count 3
             :distinct true
             :into []))

(s/def ::payload-options.button/button_template
  (s/keys :req-un
          [::payload-options.button/template_type
           ::payload-options.button/text
           ::payload-options.button/buttons]))

;; ========================== REGISTRY FOR ATTACHMENT OF TEMPLATE =============
(ns-as 'messenger.attachment
         'attachment-options.button)
(s/def ::attachment-options.button/type #{"template"})
(s/def ::attachment-options.button/payload ::payload-options.button/button_template)

(s/def ::attachment-options.button/attachment
  (s/keys :req-un
          [::attachment-options.button/type
           ::attachment-options.button/payload]))

(def generate-10-button-attachment (gen/sample (s/gen ::attachment-options.button/attachment)))
(def generate-button-attachment (gen/generate (s/gen ::attachment-options.button/attachment)))
