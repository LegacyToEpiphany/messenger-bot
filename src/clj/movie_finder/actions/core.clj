(ns movie-finder.actions.core)

(defn messenger-route
  ([key transition-fn action-fn]
   (->> (hash-map :action-fn action-fn
                  :transition-fn transition-fn)
        (hash-map key))))

(defn context [& routes]
  (let [context-map (apply conj routes)]
    (println context-map)
    (fn [entry state]
      (let [transition-fn (get-in context-map [state :transition-fn])
            next-state (transition-fn entry)]
        (if (= :start state)
          (let [action-fn (get-in context-map [state :action-fn])]
            (action-fn entry))
          (let [action-fn (get-in context-map [next-state :action-fn])]
            (action-fn entry)))
        next-state))))

(defn routes [& coll]
  (apply conj {} coll))
