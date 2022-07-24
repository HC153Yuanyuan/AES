import sbt.Keys.libraryDependencies

ThisBuild / version := "1.0"
ThisBuild / scalaVersion := "2.11.12"
ThisBuild / organization := "org.example"

val spinalVersion = "1.7.1"
val spinalCore = "com.github.spinalhdl" %% "spinalhdl-core" % spinalVersion
val spinalLib = "com.github.spinalhdl" %% "spinalhdl-lib" % spinalVersion
val spinalIdslPlugin = compilerPlugin("com.github.spinalhdl" %% "spinalhdl-idsl-plugin" % spinalVersion)
val spinalTest = "org.scalatest" %% "scalatest" % "3.2.10"

lazy val mylib = (project in file("."))
  .settings(
    name := "AES",
    libraryDependencies ++= Seq(spinalCore, spinalLib, spinalIdslPlugin,spinalTest)
  )

fork := true
