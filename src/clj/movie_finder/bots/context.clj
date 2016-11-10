(ns movie-finder.bots.context)

;; on va simuler qu'on est dans un contexte
;; 1) On crée l'atom avec la datastructure courante
;; 2) On crée la logique de (si je suis dans cet état, je lance le helper)
;; 3) On renvoie du text comme liste de film
;; 4) On crée la logique d'enregistrement de l'état courant (input, etc)
;; Objectif: je suis dans un état donné => je renvoie le helper => si réponse valide, j'affiche bravo


;; Context which describe a potential conversation type
{:context
 {:id                :simplified_movie_search
  :available_actions ["action_1"]
  :helper_id         "helper_fn_simplified_search_engine"}
 {:id                :advanced_movie_search
  :available_actions ["action_1" "action_2" "action_3" "action_4"]
  :helper_id         "helper_fn_advanced_search_movie"}
 {:id                :introduction
  :available_actions ["action_1"]
  :helper_id         "helper_present_me_the_bot"
  :default           true}}

;; Action template
{:id        "filter_movies_by_date"
 :action_fn "filtering_fn_1"
 :helper_id "helper_fn_1"}

;; Helper template
{:id                      "filter_movies_by_date_template"
 :information_template_fn "explanation_fn"
 :validate_input_fn       "validation_fn"}

;; How to describe current situation ?
{:1303278973030229
 {:current_context
                {:id :simplified_movie_search
                 :done_actions
                     [{:id :filter_movies_by_date
                       :input 1986}]}
  :done_context [{:id :introduction
                  :done_actions ["action_1"]}]}}
