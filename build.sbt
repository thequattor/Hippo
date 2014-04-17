name := "hippodb"

version := "1.0"

organization := "unicredit"

scalaVersion := "2.10.3"

scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-language:postfixOps",
  "-language:implicitConversions"
)

Revolver.settings

resolvers ++= Seq(
  "Cloudera releases" at "https://repository.cloudera.com/artifactory/libs-release",
  "RoundEights" at "http://maven.spikemark.net/roundeights"
)

libraryDependencies ++= Seq(
  "org.apache.hadoop" % "hadoop-common" % "2.4.0",
  "org.scalaz" %% "scalaz-core" % "7.0.6",
  "com.typesafe.akka" %% "akka-actor" % "2.3.1",
  "com.typesafe.akka" %% "akka-cluster" % "2.3.1",
  "com.roundeights" %% "hasher" % "1.0.0",
  "io.spray" % "spray-can" % "1.3.1",
  "io.spray" % "spray-routing" % "1.3.1",
  "org.json4s" %% "json4s-jackson" % "3.2.8"
)
