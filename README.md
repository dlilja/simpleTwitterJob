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
preconfigured for a single machine standalone Spark cluster but requires you to
enter your own Twitter and Twitter API credentials. In a distributed setting you
need to change `outputDir` and `checkpointDir` to directories which are
available to the driver and all workers in the cluster, e.g. a directory in the
`hdfs`.

The file `config.txt` is by default assumed to be in the working directory from
where you submit the Spark job. This can be changed in the file
`src/main/scala/se/danlilja/SimpleTwitterJob.scala` by changing variable
`location` to the directory containing the `config.txt` file.

You need to make sure all required libraries are available to your Spark cluster.
