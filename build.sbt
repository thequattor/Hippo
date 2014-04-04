name := "hippodb"

version := "1.0"

organization := "unicredit"

scalaVersion := "2.10.3"

scalacOptions ++= Seq("-deprecation", "-feature", "-language:postfixOps")


resolvers ++= Seq(
  "Cloudera releases" at "https://repository.cloudera.com/artifactory/libs-release",
  "RoundEights" at "http://maven.spikemark.net/roundeights"
)

libraryDependencies ++= Seq(
  "org.apache.hadoop" % "hadoop-core" % "2.0.0-mr1-cdh4.2.0",
  "org.apache.hadoop" % "hadoop-common" % "2.0.0-cdh4.2.0",
  "org.scalaz" %% "scalaz-core" % "7.0.6",
  "com.typesafe.akka" %% "akka-actor" % "2.3.1",
//  "com.typesafe.akka" % "akka-remote_2.10" % "2.3.1",
  "com.typesafe.akka" %% "akka-cluster" % "2.3.1",
//  "com.typesafe.akka" %% "akka-contrib" % "2.3.1",
  "com.roundeights" %% "hasher" % "1.0.0"
)
