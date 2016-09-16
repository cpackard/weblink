# Bidirectional Path Finder

This is a program that allows you to tell whether there's a path of links between any two URL's.
If a path does exist, the exact order of pages to visit will be printed.

## Usage

To run the program you need Java 1.6 or greater. If you just want to run the program, download the weblink.jar file and run:

    $ java -jar weblink.jar "url-1" "url-2"

Alternatively, if you'd like to run it from source, the only other additional component you need is Leiningen, which you can get here: [http://leiningen.org/](http://leiningen.org/)

Once you have the prerequisites, simply run from the command line:

    $ lein run "url-1" "url-2"

## Examples

```clojure
$ java -jar weblink.jar "https://en.wikipedia.org/wiki/Renaissance" "https://en.wikipedia.org/wiki/Swiss_cheese"
("https://en.wikipedia.org/wiki/Swiss_cheese"
 "https://en.wikipedia.org/wiki/Cheeses_of_Switzerland"
 "https://en.wikipedia.org/wiki/Cow"
 "https://en.wikipedia.org/wiki/History_of_the_world"
 "https://en.wikipedia.org/wiki/Renaissance")
$ 

```

## License

Copyright © 2016 Christian N Packard

Distributed under the MIT License
