(set-env! :resource-paths #{"clj" "java"}
          :dependencies '[[adzerk/bootlaces "0.1.11" :scope "test"]
                          [adzerk/boot-template "1.0.0"]])

(require '[adzerk.bootlaces :refer :all]
         '[adzerk.boot-ubermain :refer :all])

(def +version+ "1.0.0-SNAPSHOT")
(bootlaces! +version+)

(task-options!
 pom {:project     'adzerk/boot-ubermain
      :version     +version+
      :description "Experimental Boot task for self-contained uberjars"
      :url         "https://github.com/adzerk-oss/boot-ubermain"
      :scm         {:url "https://github.com/adzerk-oss/boot-ubermain"}
      :license     {"Eclipse Public License"
                    "http://www.eclipse.org/legal/epl-v10.html"}})
