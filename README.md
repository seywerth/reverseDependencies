#Reverse Dependency Tool
==========

Java tool to scan for reverse maven dependencies.

0. reads a list of dependencies of interest as csv
0. optionally reads reverse dependency list from caching file
0. optionally generates a list with (additional) reverse dependencies from a repository
0. optionally matches against a list of dependencies in use to generate a usage list of the dependencies of interest

Further options:
---------

* optional use name-postfixes to match dependencies of interest
* use blanks or CSV
* specify output/result file
* query a subdirectory of the repository

Future features:
---------

* match only major versions (in progress)
* scan only relevant dependencies (when matching file specified)

Usage
-----

scan the maven repo for specific usages of dependencies stated in query.txt and match against dependencies in inuse.csv
```
java -jar reverseDependencies.jar -q query.txt -m inuse.csv -r https://repo1.maven.org/maven2/ 
```

parameter info
```
$ java -jar reverseDependencies.jar
usage: java -jar reverseDependencies.jar -q query.txt -m inuse.csv -r
            https://repo1.maven.org/maven2/ -c cache.csv
 -c,--cache <arg>          path: optional file for caching repo for next
                           use; eg: nexus-cache.csv
 -i,--ignore <arg>         string: add postfixes to include for matching;
                           eg: -client,-bus-client
 -m,--match <arg>          path: dependencies currently in use to check
                           against; eg: inuse.csv
 -o,--ouput <arg>          path: resulting output of dep <- used in dep;
                           eg: depMatches.csv
 -q,--query <arg>          path: dependencies we are looking for; eg:
                           query.txt
 -r,--repository <arg>     url: repository to check; eg:
                           https://repo1.maven.org/maven2/
 -s,--subdirectory <arg>   url: repository to check; eg:
                           org/apache/maven/plugins/
 -x,--major                specifiy to only match major versions; no
                           output of used-by dependencies
specify at least a path to query or matching dependencies!
```

Build
-----

run with integrative tests:
```
mvn clean install -P IT
```
