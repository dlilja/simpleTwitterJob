# SimpleTwitterJob

A simple Apache Spark streaming job collecting data from Twitter and doing some
simple analysis of the collected data.

## Requirements

* Scala 2.11
* sbt 1.1.4
* Apache Spark 2.2.0
* twitter4j v. 4.0.6
* Apache Bahir Spark Extensions for Spark 2.2.0
* Google Gson v. 2.8.2
* Graphframes v. 0.5.0
* com.typesafe.scala-logging 2.1.2

## Building

Use `sbt` to build the project. Use the commands `compile` followed by `package`
to create a jar file to run on a Spark cluster using `spark-submit`.

## Instructions

The file `config.txt` is used to set several necessary variables. It comes
preconfigured for a standalone Spark cluster but requires you to enter your own
Twitter and Twitter API credentials. When submitting the packaged jar to run on
the standalone Spark cluster attach `config.txt` by using the `--files
/path/to/config.txt` option to `spark-submit`.

In a Yarn setting you need to change `outputDir` and `checkpointDir` in
`config.txt` to directories which are available to the driver and all workers in
the cluster, e.g. a directory in the `hdfs`. The file `config.txt` needs to be
somewhere the driver and every worker can read it, e.g. in the `hdfs`. You also
need to change the variable `opts` in the file
`src/main/scala/se/danlilja/SimpleTwitterJob.scala` to point to it.

Finally, you need to make sure all required libraries are available to your
Spark cluster.
