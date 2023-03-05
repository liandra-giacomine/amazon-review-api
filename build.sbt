val Http4sVersion          = "0.23.6"
val MunitVersion           = "0.7.29"
val LogbackVersion         = "1.2.6"
val MunitCatsEffectVersion = "1.0.6"
val CirceVersion           = "0.14.3"

Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val root = (project in file("."))
  .settings(inThisBuild(buildSettings))
  .configs(IntegrationTest)
  .settings(Defaults.itSettings)
  .settings(
    organization := "com.example",
    name         := "quickstart",
    version      := "0.0.1-SNAPSHOT",
    scalaVersion := "3.1.0",
    libraryDependencies ++= Dependencies.compile ++ Dependencies.test,
    testFrameworks += new TestFramework("munit.Framework")
  )

lazy val buildSettings = Def.settings(scalafmtOnCompile := true)
