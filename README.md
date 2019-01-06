# mpar

mpar is command line access log file parser that loads the log to MySQL database and checks if a given IP makes more than a certain number of requests for the given period of time.

The tool is built on Java and uses the following auxiliary technologies:

 * MySQL 5.1.47;
 * Hibernate 5.3.7.Final;
 * Hibernate Validator 6.0.13.Final.

# usage

 mpar accepts access log file with pipe delimiter (|).

 The parser takes three command line arguments:
  1) "startDate";
  2) "duration";
  3) "threshold".

"**startDate**" is of "yyyy-MM-dd.HH:mm:ss" format.

"**duration**" can take only "hourly", "daily" as inputs.

"**threshold**" must be a positive integer.

# how it works

The tool finds any IPs that made more than **threshold** requests starting from **startDate** to **startDate** + **duration** (one hour or one day), prints them to console and also loads them to another MySQL table with comments on why it's blocked.