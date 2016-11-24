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
(def phone-regex #"^[+][1-9]+")
(s/def ::phone-type (s/and string? #(re-matches phone-regex %)))
;TODO: implement the
(s/def :call-button/payload #{"+16505551234"})


(s/def :button/type string?)
(s/def :button/title (s/and string? #(<= (count %) 20)))
(s/def :button/payload (s/and string? #(<= (count %) 1000)))

(defmulti button-type :type)
(defmethod button-type "postback" [_]
  (s/keys :req-un [:button/title :button/type :button/payload]))
(defmethod button-type "phone_number" [_]
  (s/keys :req-un [:button/title :button/type :call-button/payload]))
(s/def :button/button (s/multi-spec button-type :type))
(gen/sample (s/gen :button/button))

;; ========================== REGISTRY FOR PAYLOAD TEMPLATE ===================
(s/def :payload/template_type #{"button"})
(s/def :payload/text (s/and string? #(<= (count %) 320)))          ;;TODO: check encoding set to UTF-8
(s/def :payload/buttons
  (s/coll-of :button/button
             :kind vector?
             :min-count 1
             :max-count 3
             :distinct true
             :into []))

(s/def :payload/button_template
  (s/keys :req-un
          [:payload/template_type
           :payload/text
           :payload/buttons]))

;; ========================== REGISTRY FOR ATTACHMENT OF TEMPLATE =============
(s/def :attachment/type #{"template"})
(s/def :attachment/payload :payload/button_template)

(s/def :attachment/attachment
  (s/keys :req-un
          [:attachment/type
           :attachment/payload]))

(def generate-10-button-attachment (gen/sample (s/gen :attachment/attachment)))
(def generate-button-attachment (gen/generate (s/gen :attachment/attachment)))
