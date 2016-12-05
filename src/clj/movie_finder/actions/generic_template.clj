(ns movie-finder.actions.generic-template
  (:require [movie-finder.actions.core :refer [messenger-route routes]]
            [movie-finder.bots.network :refer [post-messenger typing-on]]
            [movie-finder.bots.send-api :as send-api]))

(defn generic-intro-action [entry]
  (let [sender-id (keyword (str (get-in entry [:sender :id])))]
    (post-messenger sender-id :message {:text "Les templates de type génériques permettent de diffuser l'information."})
    (post-messenger sender-id :message {:text "Ils s'accompagnent éventuellement de boutons "})
    (typing-on sender-id)
    (post-messenger sender-id :message {:attachment {:type    "template"
                                                     :payload (send-api/make-generic-template
                                                                (send-api/make-element-generic-object
                                                                  {:title     "Les opérateurs télécoms visés par les cybercriminels"
                                                                   :item_url  "http://www.lesechos.fr/tech-medias/hightech/0211561511920-les-operateurs-telecoms-vises-par-les-cybercriminels-2047798.php"
                                                                   :image_url "http://www.lesechos.fr/medias/2016/12/04/2047798_les-operateurs-telecoms-vises-par-les-cybercriminels-web-tete-0211557437024_1000x533.jpg"
                                                                   :subtitle "Deutsche Telekom et l’anglais TalkTalk ont été la cible d’un même virus."
                                                                   :buttons [(send-api/make-share-button)
                                                                             (send-api/make-url-button
                                                                               {:title "L'humanité"
                                                                                :url "http://www.humanite.fr"
                                                                                :webview_height_ratio "compact"})]})
                                                                (send-api/make-element-generic-object
                                                                  {:title     "L’art de la guerre selon Huawei"
                                                                   :item_url  "http://www.lesechos.fr/tech-medias/hightech/0211557309473-lart-de-la-guerre-selon-huawei-2047654.php"
                                                                   :image_url "http://www.lesechos.fr/medias/2016/12/03/2047654_lart-de-la-guerre-selon-huawei-web-tete-0211557234684.jpg"
                                                                   :subtitle "Le fondateur Ren Zhengfei, a planifié son ascension comme un stratège militaire."
                                                                   :buttons [(send-api/make-share-button)]})
                                                                (send-api/make-element-generic-object
                                                                  {:title     "Une opération de police internationale fait tomber un réseau de cybercriminalité"
                                                                   :item_url  "http://www.lesechos.fr/tech-medias/hightech/0211557586627-une-operation-de-police-internationale-fait-tomber-un-large-reseau-de-cybercriminalite-2047631.php"
                                                                   :image_url "http://www.lesechos.fr/medias/2016/12/02/2047631_une-operation-de-police-internationale-fait-tomber-un-large-reseau-de-cybercriminalite-web-tete-0211557452830.jpg"
                                                                   :subtitle "Techniquement et juridiquement les enquêtes cyber sont difficiles."
                                                                   :buttons [(send-api/make-share-button)]})
                                                                (send-api/make-element-generic-object
                                                                  {:title     "Rémy Pflimlin, un passionné de médias"
                                                                   :item_url  "http://www.lesechos.fr/tech-medias/medias/0211561512336-remy-pflimlin-un-passionne-de-medias-portrait-2047800.php"
                                                                   :image_url "http://www.lesechos.fr/medias/2016/12/04/2047800_remy-pflimlin-un-passionne-de-medias-web-tete-0211561498050_1000x533.jpg"
                                                                   :subtitle "Un homme « formidable »"
                                                                   :buttons [(send-api/make-share-button)]}))}})))

(def generic-template-route
  (routes (messenger-route :generic-template-start
                           (fn [entry]
                             (if (= "call-button" (get-in entry [:postback :payload]))
                               :button-template-call))
                           generic-intro-action)))
