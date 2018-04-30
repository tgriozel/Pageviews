name := "pageviews"
version := "1.0.0"

// Scala
scalaVersion := "2.11.11"
scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")
javacOptions := Seq("-source", "1.8", "-target", "1.8", "-Xlint")

// Plugins
enablePlugins(JavaAppPackaging, DockerComposePlugin, PlayScala)

// Dependencies
libraryDependencies ++= Seq(
  guice,
  "com.paulgoldbaum" %% "scala-influxdb-client" % "0.6.0",
  "com.typesafe.play" %% "play-json" % "2.6.9",

  "org.scalatest" %% "scalatest" % "3.0.4" % "test",
  "org.mockito" % "mockito-core" % "2.9.0" % "test",
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % "test"
)

// Docker
testTagsToExecute := "DockerComposeTag"
dockerImageCreationTask := (publishLocal in Docker).value
dockerBaseImage := "frolvlad/alpine-oraclejdk8"
import com.typesafe.sbt.packager.docker.Cmd
dockerCommands := dockerCommands.value.flatMap{
  case cmd@Cmd("FROM",_) => List(cmd,Cmd("RUN", "apk update && apk add bash"))
  case other => List(other)
}
