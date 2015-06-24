(ns adzerk.boot-ubermain
  {:boot/export-tasks true}
  (:require [clojure.java.io      :as io]
            [boot.core            :as core]
            [boot.task.built-in   :as task]
            [boot.util            :as util]
            [boot.file            :as file]
            [boot.pod             :as pod]
            [adzerk.boot-template :refer [template]]))

(defn- pack-jar [coord]
  (core/with-pre-wrap [fs]
    (let [tmpd (core/tmp-dir!)
          jar  (io/file (boot.pod/resolve-dependency-jar (core/get-env) coord))]
      (io/copy jar (io/file tmpd (.getName jar))) 
      (-> fs (core/add-resource tmpd) core/commit!))))

(core/deftask ubermain
  "Compiles a self-contained, executable jar."

  [m main-var SYM  sym "Qualified symbol to use as the entrypoint."
   f file     NAME str "The target jar file name."]

  (assert main-var "main-var is a required argument.")

  (comp (task/uber :as-jars true)
        (core/with-pre-wrap [fs]
          (let [tmpd (core/tmp-dir!)]
            (spit (doto (io/file tmpd "adzerk/MainSploder.java") io/make-parents)
                  (slurp (io/resource "adzerk/MainSploder.java"))) 
            (-> fs (core/add-source tmpd) core/commit!)))
        (pack-jar '[org.projectodd.shimdandy/shimdandy-api "1.1.0"])
        (pack-jar '[org.projectodd.shimdandy/shimdandy-impl "1.1.0"])
        (template :paths ["adzerk/MainSploder.java"]
                  :subs {"namespace" (namespace main-var)
                         "name" (name main-var)})
        (task/javac)
        (task/jar :main 'adzerk.MainSploder)))

