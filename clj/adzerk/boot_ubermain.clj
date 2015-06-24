(ns adzerk.boot-ubermain
  {:boot/export-tasks true}
  (:require [clojure.java.io      :as io]
            [boot.core            :as core]
            [boot.task.built-in   :as task]
            [boot.util            :as util]
            [boot.file            :as file]
            [boot.pod             :as pod]
            [adzerk.boot-template :refer [template]]))

(core/deftask ubermain
  "Compiles a self-contained, executable jar."

  [m main-var SYM  sym "Qualified symbol to use as the entrypoint."
   f file     NAME str "The target jar file name."]

  (assert main-var "main-var is a required argument.")

  (comp (task/uber :as-jars true)
        (template :paths ["adzerk/MainSploder.java"]
                  :subs {"namespace" (namespace main-var)
                         "name" (name main-var)})
        (task/javac)
        (task/jar
         :file (or file "project.jar")
         :main 'adzerk.MainSploder)))

