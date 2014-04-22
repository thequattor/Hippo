import sbt._
import Keys._
import spray.revolver.RevolverPlugin._

object BuildSettings {
  val akkaVersion = "2.3.1"
  val sprayVersion = "1.3.1"

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
    ),
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,
      "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
      "com.typesafe.akka" %% "akka-contrib" % akkaVersion,
      "com.roundeights" %% "hasher" % "1.0.0",
      "net.ceedubs" %% "ficus" % "1.0.0"
    )
  )
}

object CustomBuild extends Build {
  import BuildSettings._

  lazy val root = Project(
    "hippodb-root",
    file("."),
    settings = buildSettings
  ) aggregate(client, server, http)

  lazy val common = Project(
    "hippodb-common",
    file("common"),
    settings = buildSettings
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
  ) dependsOn(common)

  lazy val client = Project(
    "hippodb-client",
    file("client"),
    settings = buildSettings
  ) dependsOn(common)

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
}