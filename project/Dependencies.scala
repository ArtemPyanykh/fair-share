import sbt._
import sbt.Keys._
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._

object Dependencies {

  import sbt.Test

  val scalazVersion = "7.1.+"

  val scalazDeps = List(
    "org.scalaz" %% "scalaz-core" % scalazVersion,
    "org.scalaz" %% "scalaz-concurrent" % scalazVersion
  )

  val scalazStreamVersion = "0.8.+"

  val scalazStreamDeps = List(
    "org.scalaz.stream" %% "scalaz-stream" % scalazStreamVersion
  )

  val scalatestVersion = "2.2.4"

  val simulacrumDeps = List(
    "com.github.mpilquist" %% "simulacrum" % "0.5.+"
  )

  val argonautVersion = "6.1"

  val argonautDeps = List(
    "io.argonaut" %% "argonaut" % argonautVersion,
    "com.github.alexarchambault" %% s"argonaut-shapeless_$argonautVersion" % "0.3.1"
  )

  val scalatestDeps = List(
    "org.scalatest" %% "scalatest" % scalatestVersion % Test
  )

  val http4sVersion = "0.12.+"
  val http4sDeps = List(
    "org.http4s" %% "http4s-dsl" % http4sVersion, // to use the core dsl
    "org.http4s" %% "http4s-blaze-server" % http4sVersion, // to use the blaze backend
    "org.http4s" %% "http4s-argonaut" % http4sVersion // to auto-derive EntityCoders from Argonaut JSON codecs
  )

  val shapelessVersion = "2.3.+"
  val shapelessDeps = List(
    "com.chuusai" %% "shapeless" % shapelessVersion
  )

  val loggingDeps = Seq(
    "org.slf4j" % "slf4j-api" % "1.7.18",
    "ch.qos.logback" % "logback-classic" % "1.1.6",
    "com.typesafe.scala-logging" % "scala-logging_2.11" % "3.1.0"
  )

  val widokVersion = "0.2.+"
  val scalaJSDepsCombined = libraryDependencies ++= Seq(
    "io.github.widok" %%% "widok" % widokVersion
  )
}
