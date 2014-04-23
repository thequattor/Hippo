import sbt._
import Keys._
import spray.revolver.RevolverPlugin._
import sbtassembly.Plugin._
import AssemblyKeys._

object BuildSettings {
  val akkaVersion = "2.3.1"
  val sprayVersion = "1.3.1"
  val cdhVersion = "4.6.0"

  val buildSettings = Defaults.defaultSettings ++ Seq(
    organization := "unicredit",
    version := "1.0",
    scalaVersion := "2.10.3",
    scalacOptions ++= Seq(
      "-deprecation",
      "-feature",
      "-language:postfixOps",
      "-language:implicitConversions"
    ),
    resolvers ++= Seq(
      "RoundEights" at "http://maven.spikemark.net/roundeights"
    )
  )
}

object CustomBuild extends Build {
  import BuildSettings._

  lazy val root = Project(
    "hippodb-root",
    file("."),
    settings = buildSettings
  ) aggregate(client, server, http, hbase, retriever)

  lazy val common = Project(
    "hippodb-common",
    file("common"),
    settings = buildSettings ++ Seq(
      libraryDependencies ++= Seq(
        "com.roundeights" %% "hasher" % "1.0.0"
      )
    )
  )

  lazy val akkaCommon = Project(
    "hippodb-akka-common",
    file("akka-common"),
    settings = buildSettings ++ Seq(
      libraryDependencies ++= Seq(
        "com.typesafe.akka" %% "akka-actor" % akkaVersion,
        "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
        "com.typesafe.akka" %% "akka-contrib" % akkaVersion,
        "net.ceedubs" %% "ficus" % "1.0.0"
      )
    )
  )

  lazy val server = Project(
    "hippodb-server",
    file("server"),
    settings = buildSettings ++ Seq(
      libraryDependencies ++= Seq(
        "org.apache.hadoop" % "hadoop-common" % "2.4.0",
        "org.scalaz" %% "scalaz-core" % "7.0.6",
        "com.google.guava" % "guava" % "16.0.1"
      )
    ) ++ Revolver.settings
  ) dependsOn(common, akkaCommon)

  lazy val client = Project(
    "hippodb-client",
    file("client"),
    settings = buildSettings
  ) dependsOn(common, akkaCommon)

  lazy val http = Project(
    "hippodb-http",
    file("http"),
    settings = buildSettings ++ Seq(
      libraryDependencies ++= Seq(
        "io.spray" % "spray-can" % sprayVersion,
        "io.spray" % "spray-routing" % sprayVersion,
        "org.json4s" %% "json4s-jackson" % "3.2.8"
      )
    ) ++ Revolver.settings
  ) dependsOn(client)

  lazy val retriever = Project(
    "hippodb-retriever",
    file("retriever"),
    settings = buildSettings ++ Seq(
      libraryDependencies ++= Seq(
       "org.apache.hadoop" % "hadoop-common" % "2.4.0",
       "com.github.scopt" %% "scopt" % "3.2.0"
      )
    )
  )

  lazy val hbase = Project(
    "hippodb-hbase-sync",
    file("hbase-sync"),
    settings = buildSettings ++ assemblySettings ++ Seq(
      resolvers ++= Seq(
        "Cloudera releases" at "https://repository.cloudera.com/artifactory/libs-release",
        "Concurrent Maven Repo" at "http://conjars.org/repo"
      ),
      libraryDependencies ++= Seq(
        "com.twitter" %% "scalding-core" % "0.9.1",
        "org.apache.hadoop" % "hadoop-core" % s"2.0.0-mr1-cdh$cdhVersion" % "provided",
        "org.apache.hadoop" % "hadoop-common" % s"2.0.0-cdh$cdhVersion" % "provided",
        "org.apache.hbase" % "hbase" % s"0.94.15-cdh$cdhVersion"// % "provided"
      ),
      mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
        {
          case PathList("META-INF", _*) => MergeStrategy.discard
          case _ => MergeStrategy.last
        }
      },
      mainClass in assembly := Some("com.twitter.scalding.Tool")
    )
  ) dependsOn(common)
}