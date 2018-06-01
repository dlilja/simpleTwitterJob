// Copyright [yyyy] [name of copyright owner]

// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at

// http://www.apache.org/licenses/LICENSE-2.0

// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

import Dependencies._

lazy val root = (project in file("."))
  .settings(
    name         := "simpleTwitterJob",
    scalaVersion := "2.11.12",
    version      := "0.1.0-SNAPSHOT",
    description  := "Simple Twitter streaming and analysis using Apache Spark.",
    organization := "se.danlilja",
    organizationName := "Dan Lilja",
    organizationHomepage := Some(url("http://www.danlilja.se")),
    licenses += "Apache license, version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0"),
    libraryDependencies ++= projectDeps,
    resolvers += Resolver.bintrayRepo("spark-packages", "maven")
    )
