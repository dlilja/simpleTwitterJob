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

import sbt._

object Dependencies {
  lazy val sparkVersion = "2.2.0"
  lazy val gsonVersion = "2.8.2"
  lazy val twitter4jVersion = "4.0.6"
  lazy val graphframesVersion = "0.5.0-spark2.1-s_2.11"
  lazy val bahirVersion = "2.2.0"

  val sparkCore = "org.apache.spark" %% "spark-core" % sparkVersion
  val sparkSql = "org.apache.spark" %% "spark-sql" % sparkVersion
  val sparkStreaming = "org.apache.spark" %% "spark-streaming" % sparkVersion
  val graphFrames = "graphframes" % "graphframes" % graphframesVersion
  val twitter4jcore = "org.twitter4j" % "twitter4j-core" % twitter4jVersion
  val twitter4jstream = "org.twitter4j" % "twitter4j-stream" % twitter4jVersion
  val twitter4jasync = "org.twitter4j" % "twitter4j-async" % twitter4jVersion
  val twitter4jmedia = "org.twitter4j" % "twitter4j-media-support" % twitter4jVersion
  val gson = "com.google.code.gson" % "gson" % gsonVersion
  val bahir = "org.apache.bahir" %% "spark-streaming-twitter" % bahirVersion

  val projectDeps = Seq(
    sparkCore,
    sparkSql,
    sparkStreaming,
    gson,
    twitter4jcore,
    twitter4jstream,
    twitter4jasync,
    twitter4jmedia,
    graphFrames,
    bahir
  )
}
