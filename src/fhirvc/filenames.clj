(ns fhirvc.filenames)

(defn comparison [a b]
  (str a "_" b  "_page.html"))

(defn comparison-dir [a b]
  (str a "_" b "/"))

(defn definition [[name _]]
  (str name ".html"))
  
(defn def-in-comparison [comp sem-diff]
  (str (comparison-dir (:old-version comp) (:new-version comp))
       "/"
       (definition sem-diff)))
