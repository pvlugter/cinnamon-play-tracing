lazy val cinnamonPlayTracing = project
  .in(file("."))
  .aggregate(frontend, service, backend, tracedws)

lazy val frontend = project
  .in(file("frontend"))
  .enablePlugins(PlayScala, Cinnamon)
  .dependsOn(tracedws)
  .settings(
    scalaVersion := "2.12.3",
    libraryDependencies += guice,
    libraryDependencies += ws,
    libraryDependencies += Cinnamon.library.cinnamonOpenTracingZipkin
  )

lazy val service = project
  .in(file("service"))
  .enablePlugins(PlayScala, Cinnamon)
  .settings(
    scalaVersion := "2.12.3",
    libraryDependencies += guice,
    libraryDependencies += "com.typesafe.akka" %% "akka-remote" % "2.5.4",
    libraryDependencies += Cinnamon.library.cinnamonOpenTracingZipkin
  )

lazy val backend = project
  .in(file("backend"))
  .enablePlugins(Cinnamon)
  .settings(
    scalaVersion := "2.12.3",
    libraryDependencies += "com.typesafe.akka" %% "akka-remote" % "2.5.4",
    libraryDependencies += Cinnamon.library.cinnamonOpenTracingZipkin,
    cinnamon in run := true,
    connectInput in run := true // we wait on stdin
  )

lazy val tracedws = project
  .in(file("tracedws"))
  .settings(
    scalaVersion := "2.12.3",
    libraryDependencies += ws,
    libraryDependencies += Cinnamon.library.cinnamonOpenTracing
  )
