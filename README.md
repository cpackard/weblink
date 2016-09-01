# weblink

This is a program that allows you to tell whether there's a path of URL's between any two links.
If a path does exist, the exact order of pages to visit will be printed.

## Usage

To run the file you need Java 1.6 or greater and Leiningen, which you can get here: [Leiningen](http://http://leiningen.org/)

Once you have the prerequisites, simply run from the command line:

    $ lein run "link-1" "link-2"

## Examples

```clojure
$ lein run "https://en.wikipedia.org/wiki/Renaissance" "https://en.wikipedia.org/wiki/Swiss_cheese"
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
