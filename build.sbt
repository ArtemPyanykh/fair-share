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
  name := """fair-share""",
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
  persistLauncher := true
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

lazy val backend = project.in(file("backend"))
  .settings(buildSettings)
  .settings(scalacOptions ++= compilerOptions)
  .settings(
    libraryDependencies ++=
      scalazDeps ++ scalatestDeps ++ simulacrumDeps ++ http4sDeps ++ shapelessDeps ++ argonautDeps ++ loggingDeps
  )
  .settings(libraryDependencies ++= compilerPlugins)

lazy val frontend = project.in(file("frontend"))
  .settings(buildSettings)
  .settings(libraryDependencies ++= compilerPlugins)
  .enablePlugins(SbtWeb)
  .dependsOn(js)
  .settings(
    resourceGenerators in Assets <+= Def.task {
      val appJs = (fastOptJS in (js, Compile)).value
      appJs.data :: Nil
    },
    (mappings in (Assets, resources)) <<= (mappings in (Assets, resources)).map { theMappings =>
      theMappings.map { case (file, string) =>
          string match {
            case "fair-share-fastopt.js" => file -> (s"js/$string")
            case _ => file -> string
          }
      }
    },
    compile in Compile <<= (compile in Compile).dependsOn(WebKeys.assets in Assets),
    unmanagedClasspath in Compile += (WebKeys.public in Assets).value
  )

lazy val js = project.in(file("frontend/js"))
  .settings(buildSettings)
  .settings(scalacOptions ++= compilerOptions ++ Seq("-Xelide-below", annotation.elidable.ALL.toString))
  .settings(scalaJSDepsCombined)
  .enablePlugins(ScalaJSPlugin)
  .settings(jsSettings)
