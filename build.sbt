
name := "scalafxmsuic"

organization := "scalafxmsuic"

version := "1.0"

scalaVersion := "2.10.3"


resolvers ++= Seq(
    "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
)


libraryDependencies ++= Seq(
  "org.scalafx" %% "scalafx" % "1.0.0-M5",
  "org.scalatest" % "scalatest_2.10" % "1.9.2",
  "com.typesafe.akka" %% "akka-actor" % "2.2.1"
)

unmanagedJars in Compile += Attributed.blank(
    file(scala.util.Properties.javaHome) / "lib" / "jfxrt.jar")

fork in run := true

// set the main class for the main 'run' task
// change Compile to Test to set it for 'test:run'
mainClass in (Compile, run) := Some("scalafxmusic.MusicPlayer")
