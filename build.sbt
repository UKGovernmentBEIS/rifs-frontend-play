name := "rifs-frontend"

scalaVersion := "2.11.8"

lazy val `rifs-frontend` = (project in file("."))
  .enablePlugins(PlayScala)
  .disablePlugins(PlayLayoutPlugin)
  .enablePlugins(GitVersioning)
  .enablePlugins(GitBranchPrompt)

git.useGitDescribe := true

routesImport ++= Seq(
  "models._",
  "models.PlayBindings._",
  "com.wellfactored.playbindings.ValueClassUrlBinders._"
)

PlayKeys.devSettings := Seq("play.server.http.port" -> "9000")

// need this because we've disabled the PlayLayoutPlugin. without it twirl templates won't get
// re-compiled on change in dev mode
PlayKeys.playMonitoredFiles ++= (sourceDirectories in (Compile, TwirlKeys.compileTemplates)).value

libraryDependencies ++= Seq(
  ws,
  "joda-time" % "joda-time" % "2.7",
  "org.joda" % "joda-convert" % "1.7",
  "org.typelevel" %% "cats" % "0.7.2",
  "com.github.melrief" %% "pureconfig" % "0.1.6",
  "com.wellfactored" %% "play-bindings" % "1.1.0",

  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test
)


