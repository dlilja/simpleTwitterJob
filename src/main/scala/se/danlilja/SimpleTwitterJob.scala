// Copyright 2018 Dan Lilja

// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at

// http://www.apache.org/licenses/LICENSE-2.0

// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package se.danlilja.simpletwitterjob

import scala.io._
import java.io.FileNotFoundException

import twitter4j._
import twitter4j.auth.Authorization
import twitter4j.conf.ConfigurationBuilder
import twitter4j.auth.OAuthAuthorization

import org.apache.spark.SparkFiles
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming._
import org.apache.spark.streaming.dstream._
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.receiver.Receiver
import org.apache.spark.streaming.twitter._

import com.google.gson.Gson

import org.graphframes._

object SimpleTwitterJob {
  // Container for all options
  case class TwitterOpts(consumerKey: String,
                         consumerSecret: String,
                         accessToken: String,
                         accessTokenSecret: String,
                         timeout: Int,
                         outputDir: String,
                         checkpointDir: String)

  // Parser for the config file
  def parseOpts(input: BufferedSource): TwitterOpts = {
    val contents = input.getLines.toList.filterNot(x => x.isEmpty || x.startsWith("#"))
    input.close
    val fields = contents.map(x => x.split("=")).map(x => x(1))
    TwitterOpts(fields(0),
                fields(1),
                fields(2),
                fields(3),
                fields(4).toInt * 1000,
                fields(5),
                fields(6))
  }

  def main(args: Array[String]) {
    // Create SparkSession
    val spark = SparkSession
      .builder
      .appName("TwitterAnalysis")
      .getOrCreate()
    import spark.implicits._
    val sc = spark.sparkContext

    val location = System.getProperty("user.dir")
    val opts = sc.broadcast(parseOpts(Source.fromFile(location + "/config.txt")))

    System.setProperty("twitter4j.oauth.consumerKey", opts.value.consumerKey)
    System.setProperty("twitter4j.oauth.consumerSecret", opts.value.consumerSecret)
    System.setProperty("twitter4j.oauth.accessToken", opts.value.accessToken)
    System.setProperty("twitter4j.oauth.accessTokenSecret", opts.value.accessTokenSecret)

    // Setup streaming environment
    val streamInterval = new Duration(5 * 1000)

    val ssc = new StreamingContext(sc, streamInterval)
    val twitterStream = TwitterUtils.createStream(ssc, None)
    val twitterJson = twitterStream.map(x => {
                                          val gson = new Gson()
                                          gson.toJson(x)
                                        })
    val partitionsPerInterval = 1

    twitterJson.foreachRDD(
      (rdd, time) => {
        if (rdd.count() > 0) {
          val outputRDD = rdd.repartition(partitionsPerInterval) // repartition
          outputRDD.saveAsTextFile(opts.value.outputDir + "tweets_" + time.milliseconds.toString) // write to file
        }
      }
    )

    // Streaming
    ssc.start()
    ssc.awaitTerminationOrTimeout(opts.value.timeout)
    ssc.stop(stopSparkContext = false, stopGracefully = true)

    // Loading and analyzing the data
    // Create a DataFrame to count the number of tweets.
    val tweetsDF = spark.read.json(opts.value.outputDir + "tweets_*/part-00000")
    tweetsDF.cache()
    val tweetsCountsDF = tweetsDF.select("*")
      .groupBy("id", "user.screenName").count().sort($"count".desc)

    // Create DataSet of relevant information so we can use map(...) on it to count characters.
    val tweetsDS = tweetsDF.select("id", "user.screenName", "text").as[(Long, String, String)]

    // Create new DataSet by counting the number of characters in each tweet.
    // Note that this is a stupid count. A better count would also handle retweets and replies.
    val charCountsDS = tweetsDS.map( {
                                      case (id, screenName, text) =>
                                        (id, screenName, text.length())
                                        //case x => x
                                    })
      .sort($"_3".desc).select($"_1".as("id"), $"_2".as("screenName"), $"_3".as("charCount"))

    // Let's see who had the most number of characters tweeted...
    println("Number of characters tweeted by user:")
    charCountsDS.show(5)
    // ... and compare it to who had the most number of tweets.
    println("Number of tweets by user:")
    tweetsCountsDF.show(5)

    // Next we will analyze the retweet network. This will use GraphFrames.
    // Create a DataFrame containing only retweets
    val retweetsDF = tweetsDF.filter("retweetedStatus is not null")
      .select($"user.screenName".as("retweeter"),
              $"retweetedstatus.user.screenName".as("retweetee"))
    retweetsDF.cache()

    // Create DataFrames corresponding to vertices and directed edges respectively.
    val retweetsVertices = retweetsDF.select($"retweeter")
      .union(retweetsDF.select($"retweetee")).distinct().toDF("id")
    val retweetsEdges = retweetsDF.toDF("src", "dst")

    // Creating the GraphFrame.
    spark.sparkContext.setCheckpointDir(opts.value.checkpointDir)
    val graph = GraphFrame(retweetsVertices, retweetsEdges)
    graph.cache()

    // Who was retweeted the most?
    println("Who was retweeted the most?")
    graph.inDegrees.orderBy($"inDegree".desc).show(10)

    println("Who retweeted the most?")
    // Who retweeted the most?
    graph.outDegrees.orderBy($"outDegree".desc).show(10)

    println("Who retweeted each other?")
    // Find people who retweeted each other
    graph.find("(a) - [e1] -> (b); (b) - [e2] -> (a)").show(10)
    graph.find("(a) - [e1] -> (b); (b) - [e2] -> (a)").where($"a" =!= $"b").show(10)

    // // Compute connected components
    // val components = graph.connectedComponents.run
    // components.cache()

    // // How many connected components are there?
    // println("Number of connected components: " + components.select("component").distinct.count())

    // // How many people belong to each connected component?
    // println("How many users belong to each connected component?")
    // components.groupBy("component").count().orderBy($"count".desc).show(10)

    // // What is the PageRank of each user?
    // println("Top 10 pageranks:")
    // graph.pageRank.resetProbability(0.15).tol(0.01).run.vertices
    //   .orderBy($"pagerank".desc).show(10)

    spark.stop()
  }
}
