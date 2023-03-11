ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

lazy val root = (project in file("."))
  .settings(
    name := "kafka-stream-tutorial"
  )

libraryDependencies ++= Seq(
  "org.apache.kafka" %% "kafka-streams-scala" % "3.4.0",
  "org.apache.kafka" % "kafka-streams" % "3.4.0",
  "org.apache.kafka" % "kafka-clients" % "3.4.0",
  "org.apache.kafka" % "connect-api" % "3.4.0",
  "org.slf4j" % "slf4j-api" % "2.0.6",
  "org.slf4j" % "slf4j-simple" % "2.0.6",
  "org.scalatest" %% "scalatest" % "3.2.15" % Test
)