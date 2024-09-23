val scala3Version = "3.5.0"

lazy val root = project
  .in(file("."))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    organization := "org.felher",
    name := "beminar-cheat-sheet",
    version := "1.0.0",
    scalaJSUseMainModuleInitializer := true,
    scalaVersion := scala3Version,
    scalacOptions ++= Seq(
      "-language:strictEquality",
      "-source:future",
      "-feature",
      "-deprecation",
      "-Xkind-projector:underscores",
      "-Wsafe-init",
      "-Xmax-inlines:256",
      "-Wunused:all",
      "-Wvalue-discard",
      "-explain"
    ),

    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "2.8.0",
    libraryDependencies += "com.raquo" %%% "laminar" % "17.1.0",
    libraryDependencies += "org.felher" %%% "beminar" % "1.1.0",
  )
