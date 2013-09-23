
name := "scalafx-sample"

organization := "scalafx-sample"

version := "1.0"

scalaVersion := "2.10.2"

libraryDependencies ++= Seq(
  "org.scalafx" %% "scalafx" % "1.0.0-M5",
  "org.scalatest" % "scalatest_2.10" % "1.9.2" % "test" //http://www.scalatest.org/download
)

unmanagedJars in Compile += Attributed.blank(
    file(scala.util.Properties.javaHome) / "lib" / "jfxrt.jar")

fork in run := true

// set the main class for the main 'run' task
// change Compile to Test to set it for 'test:run'
mainClass in (Compile, run) := Some("scalafx_sample.HelloScalaFX")
