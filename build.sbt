name := "ScalaProject"

version := "1.0.0"

scalaVersion := "2.13.12"

organization := "com.example"

// Дependencies
libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.2.17" % Test,
  "org.scala-lang.modules" %% "scala-parser-combinators" % "2.3.0",
  "com.typesafe.akka" %% "akka-actor" % "2.6.20"
)

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-unchecked",
  "-Xlint",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-unused",
  "-Ywarn-value-discard"
)

// Настройки для генерации документации
enablePlugins(SiteScaladocPlugin)

// Описание проекта
description := "Comprehensive Scala project demonstrating best practices and language features"

