(ns movie-finder.bots.spec
  (:require [clojure.spec :as s]
            [clojure.spec.gen :as gen]))

;; ========================== SPEC HELPERS ====================================
(defn ns-as
  "Creates a namespace 'n' (if non existant) and then aliases it to 'a'"
  [n a]
  (create-ns n)
  (alias a n))

;; ========================== REGISTRY FOR ATTACHMENT OF TEMPLATE =============
(ns-as 'messenger.attachment
         'attachment-options.button)
(s/def ::attachment-options.button/type #{"template"})

(s/def ::attachment-options.button/attachment
  (s/keys :req-un
          [::attachment-options.button/type
           ::payload-options.button/button-template]))

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

(s/def ::payload-options.button/button-template
  (s/keys :req-un
          [::payload-options.button/template_type
           ::payload-options.button/text
           ::payload-options.button/buttons]))

;; ========================== REGISTRY FOR BUTTON =============================
(ns-as 'messenger.button
       'button-options.button)
(s/def ::button-options.button/type #{"postback"})
(s/def ::button-options.button/title (s/and string? #(<= (count %) 20)))
(s/def ::button-options.button/payload (s/and string? #(<= (count %) 1000)))

;; Postback Button spec
(s/def ::button-options.button/postback-button
  (s/keys :req-un
          [::button-options.button/type
           ::button-options.button/title
           ::button-options.button/payload]))

(def generate-postback-button (gen/generate (s/gen ::button-options.button/postback-button)))
(gen/sample (s/gen ::button-options.button/postback-button) 10)


