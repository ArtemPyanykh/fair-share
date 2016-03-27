import scalariform.formatter.preferences._

import Dependencies._

initialize := {
  val required = "1.8"
  val current = sys.props("java.specification.version")
  assert(current == required, s"Unsupported JDK: java.specification.version $current != $required")
}

resolvers ++= List(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)

lazy val buildSettings = Seq(
  version := "1.0-SNAPSHOT",
  scalaVersion := "2.11.8"
)

lazy val compilerOptions = Seq(
  "-deprecation",
  "-encoding", "UTF-8", // yes, this is 2 args
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-unchecked",
  //  "-Xfatal-warnings",
  //  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code", // N.B. doesn't work well with the ??? hole
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Xfuture",
  "-target:jvm-1.8"
)

lazy val compilerPlugins = Seq(
  compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
  compilerPlugin("org.spire-math" %% "kind-projector" % "0.7.1")
)

lazy val jsSettings = Seq(
  scalaJSUseRhino in Global := false,
  persistLauncher in Test := false,
  persistLauncher in Compile := true
)

scalariformSettings

ScalariformKeys.preferences := ScalariformKeys.preferences.value
  .setPreference(DoubleIndentClassDeclaration, true)

lazy val formatCode = taskKey[Unit]("Format all code")

formatCode := {
  ScalariformKeys.format.in(Compile).value
  ScalariformKeys.format.in(Test).value
}

cancelable in Global := true

lazy val root = project.in(file("."))
  .aggregate(backend, frontend)
  .settings(
    name := "fair-share"
  )
  .settings(buildSettings)

lazy val backend = project.in(file("backend"))
  .settings(buildSettings)
  .settings(scalacOptions ++= compilerOptions)
  .settings(
    libraryDependencies ++=
      scalazDeps ++ scalatestDeps ++ simulacrumDeps ++ http4sDeps ++ shapelessDeps ++ argonautDeps ++ loggingDeps ++ configDeps
  )
  .settings(libraryDependencies ++= compilerPlugins)

lazy val frontend = project.in(file("frontend"))
  .settings(buildSettings)
  .settings(scalacOptions ++= compilerOptions ++ Seq("-Xelide-below", annotation.elidable.ALL.toString))
  .settings(libraryDependencies ++= compilerPlugins)
  .settings(frontendDepsCombined)
  .enablePlugins(ScalaJSPlugin)
  .settings(jsSettings)
