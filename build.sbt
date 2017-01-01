import sbtbuildinfo.BuildInfoPlugin.autoImport._

name := "rifs-frontend-play"

scalaVersion := "2.11.8"

lazy val `rifs-frontend-play` = (project in file("."))
  .enablePlugins(PlayScala)
  .disablePlugins(PlayLayoutPlugin)
  .enablePlugins(GitVersioning)
  .enablePlugins(GitBranchPrompt)
  .enablePlugins(BuildInfoPlugin)

git.useGitDescribe := true

scalacOptions ++= Seq("-feature")

routesImport ++= Seq(
  "models._",
  "models.PlayBindings._",
  "controllers._",
  "com.wellfactored.playbindings.ValueClassUrlBinders._",
  "controllers.RefinedBinders._"
)

buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion)
buildInfoPackage := "rifs.frontend.buildinfo"
buildInfoOptions ++= Seq(BuildInfoOption.ToJson, BuildInfoOption.BuildTime)

PlayKeys.devSettings := Seq("play.server.http.port" -> "9000")
javaOptions := Seq(
  "-Dconfig.file=src/main/resources/development.application.conf",
  "-Dlogger.file=src/main/resources/development.logger.xml"
)

// need this because we've disabled the PlayLayoutPlugin. without it twirl templates won't get
// re-compiled on change in dev mode
PlayKeys.playMonitoredFiles ++= (sourceDirectories in(Compile, TwirlKeys.compileTemplates)).value

libraryDependencies ++= Seq(
  ws,
  "joda-time" % "joda-time" % "2.9.6",
  "org.joda" % "joda-convert" % "1.8.1",
  "org.typelevel" %% "cats-core" % "0.8.1",
  "com.github.melrief" %% "pureconfig" % "0.4.0",
  "com.wellfactored" %% "play-bindings" % "2.0.0",
  "com.beachape" %% "enumeratum" % "1.5.2",
  "com.beachape" %% "enumeratum-play-json" % "1.5.2",
  "eu.timepit" %% "refined" % "0.6.1",
  "com.lunaryorn" %% "play-json-refined" % "0.1",

  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test,
  "org.jsoup" % "jsoup" % "1.9.2" % Test
)


