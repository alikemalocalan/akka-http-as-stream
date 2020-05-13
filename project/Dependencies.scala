import sbt._

object Dependencies {
  lazy val versions = new {
    val akka = "2.6.4"
    val circeVersion = "0.13.0"
  }

  lazy val tests = Seq("org.scalatest" %% "scalatest" % "3.1.1" % Test)

  lazy val akkaDepends = Seq(
    "de.heikoseeberger" %% "akka-http-circe" % "1.31.0",
    "com.typesafe.akka" %% "akka-stream" % versions.akka,
    "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"
  )

  val circeParsers = Seq(
    "io.circe" %% "circe-generic" % versions.circeVersion,
    "io.circe" %% "circe-parser" % versions.circeVersion,
    "io.circe" %% "circe-generic-extras" % versions.circeVersion
  )

}
