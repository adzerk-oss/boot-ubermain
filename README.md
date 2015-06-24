# boot-ubermain

**Experimental** boot task for creating standalone uberjars without AOT and
  without exploding dependencies (or otherwise taxing the FileSet).

[![Clojars Project][1]][2]

## Usage

    $ boot -d org.clojure/clojure ubermain -m clojure.core/println
    $ java -jar target/project.jar

## License

Copyright © 2015 Adzerk

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

[1]: http://clojars.org/adzerk/boot-ubermain/latest-version.svg
[2]: http://clojars.org/adzerk/boot-ubermain
[3]: https://docs.docker.com/reference/builder/
