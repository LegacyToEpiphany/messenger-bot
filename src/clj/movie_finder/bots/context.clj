(ns movie-finder.bots.context)


;; Context which describe a potential conversation type
{:context
 {:id :simplified_movie_search
  :available_actions ["action_1"]
  :helper_fn "helper_fn_simplified_search_engine"}
 {:id :advanced_movie_search
  :available_actions ["action_1" "action_2" "action_3" "action_4"]
  :helper_fn "helper_fn_advanced_search_movie"}
 {:id :introduction
  :available_actions ["action_1"]
  :helper_fn "helper_present_me_the_bot"
  :default true}}

;; Action template
{:id "filter_movies_by_date"
 :action_fn "filtering_fn_1"
 :helper_fn "helper_fn_1"}

;; Helper template
{:id "filter_movies_by_date_template"
 :explanation_fn "explanation_fn"
 :validation_fn "validation_fn"}

;; How to describe current situation ?
{:123456789
 {:current_context
                {:id :simplified_movie_search
                 :done_actions
                     [{:id :filter_movies_by_date
                       :input 1986}]}
  :done_context [{:id :introduction
                  :done_actions ["action_1"]}]}}
