(ns movie-finder.bots.send-api
  (:require [movie-finder.bots.spec :as spec]
            [clojure.spec :as s]))

;; TODO Spec functions
;; TODO Integrate Messenger Extension
;; TODO Make the URL Button with URL Extension a true standard
;; TODO Make buy button
;; TODO Make Log-in button
;; TODO Make Log-out button
;; TODO Make Default button work
;; TODO Make Default_action object

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

(defn make-share-button []
  (let [button {:type    "element_share"}
        parsed (s/conform :button/button button)]
    (if (= parsed ::s/invalid)
      (throw (ex-info "Invalid input" (s/explain-data :button/button button)))
      button)))
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
        parsed (s/conform :button-template/button_template button-template)]
    (if (= parsed ::s/invalid)
      (throw (ex-info "Invalid input" (s/explain-data :button-template/button_template button-template)))
      button-template)))



;;;;;;;;;;;;;;;;;;;;;;; Generic Template ;;;;;;;;;;;;;;;;;;;
;;See:
;; https://developers.facebook.com/docs/messenger-platform/send-api-reference/generic-template

(defn make-generic-template [& elements]
  (let [generic-template
        {:template_type "generic"
         :elements       (vec elements)}
        parsed (s/conform :generic-template/generic_template generic-template)]
    (if (= parsed ::s/invalid)
      (throw (ex-info "Invalid input" (s/explain-data :generic-template/generic_template generic-template)))
      generic-template)))

(defn make-element-generic-object [{:keys [title item_url default_action image_url subtitle buttons]}]
  (let [element-object
        {:title          title
         :item_url       item_url
         :default_action default_action
         :image_url      image_url
         :subtitle       subtitle
         :buttons        buttons}
        filtered-element-object (into {} (remove #(nil? (val %)) element-object))
        parsed (s/conform :generic-template/element filtered-element-object)]
    (if (= parsed ::s/invalid)
      (throw (ex-info "Invalid input" (s/explain-data :generic-template/element filtered-element-object)))
      filtered-element-object)))



;;;;;;;;;;;;;;;;;;;;;;; List Template ;;;;;;;;;;;;;;;;;;;
;;See:
;; https://developers.facebook.com/docs/messenger-platform/send-api-reference/list-template

(defn make-list-template [{:keys [top_element_style elements buttons]}]
  (let [list-template
        {:template_type "list"
         :top_element_style top_element_style
         :elements elements
         :buttons buttons}
        filtered-element-object (into {} (remove #(nil? (val %)) list-template))
        parsed (s/conform :list-template/list-template filtered-element-object)]
    (if (= parsed ::s/invalid)
      (throw (ex-info "Invalid input" (s/explain-data :list-template/list-template filtered-element-object)))
      filtered-element-object)))

(defn make-element-list-object [{:keys [title item_url default_action image_url subtitle buttons]}]
  (let [element-object
        {:title          title
         :item_url       item_url
         :default_action default_action
         :image_url      image_url
         :subtitle       subtitle
         :buttons        buttons}
        filtered-element-object (into {} (remove #(nil? (val %)) element-object))
        parsed (s/conform :list-template/element filtered-element-object)]
    (if (= parsed ::s/invalid)
      (throw (ex-info "Invalid input" (s/explain-data :list-template/element filtered-element-object)))
      filtered-element-object)))
