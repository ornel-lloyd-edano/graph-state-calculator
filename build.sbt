name := "OrnelLloydAssignment"

version := "1.0"

scalaVersion := "2.13.0"

val akkaVersion = "2.6.15"
val akkaHttpVersion = "10.2.4"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
  "org.scalatest" %% "scalatest" % "3.1.4" % Test,
  "org.scalamock" %% "scalamock" % "5.1.0" % Test
)

import com.typesafe.sbt.packager.docker._
enablePlugins(JavaAppPackaging)
packageName in Docker := "ornel-state-calculator"
dockerBaseImage := "openjdk:8-jre-alpine"
dockerCommands ++= Seq(  Cmd("USER", "root"),  ExecCmd("RUN", "apk", "add", "--no-cache", "bash"))
