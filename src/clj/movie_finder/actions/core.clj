(ns movie-finder.actions.core)

(defn messenger-route
  ([key route-type transition-fn action-fn]
   (->> (hash-map :action-fn action-fn
                  :transition-fn transition-fn
                  :type route-type)
        (hash-map key))))

(defn context [& routes]
  (let [context-map (apply conj routes)]
    (println context-map)
    (fn [entry state type]
      (if-not (contains? (get-in context-map [state :type]) type)
        state
        (let [transition-fn (get-in context-map [state :transition-fn])
              next-state (transition-fn entry)
              action-fn (get-in context-map [next-state :action-fn])]
          (action-fn entry)
          next-state)))))

(defn routes [& coll]
  (apply conj {} coll))
