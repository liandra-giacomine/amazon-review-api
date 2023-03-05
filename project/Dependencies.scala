import sbt._
object Dependencies {
  val Http4sVersion          = "0.23.6"
  val MunitVersion           = "0.7.29"
  val LogbackVersion         = "1.2.6"
  val MunitCatsEffectVersion = "1.0.6"
  val CirceVersion           = "0.14.3"

  val compile = Seq(
    "org.http4s"      %% "http4s-ember-server" % Http4sVersion,
    "org.http4s"      %% "http4s-ember-client" % Http4sVersion,
    "org.http4s"      %% "http4s-circe"        % Http4sVersion,
    "org.http4s"      %% "http4s-dsl"          % Http4sVersion,
    "io.circe"        %% "circe-generic"       % CirceVersion,
    "io.circe"        %% "circe-parser"        % CirceVersion,
    "org.scalactic"   %% "scalactic"           % "3.2.15",
    "ch.qos.logback"   % "logback-classic"     % LogbackVersion,
    "com.google.guava" % "guava"               % "28.1-jre"
  )

  val test = Seq(
    "org.scalameta"     %% "munit"               % MunitVersion,
    "org.typelevel"     %% "munit-cats-effect-3" % MunitCatsEffectVersion,
    "org.scalatest"     %% "scalatest"           % "3.2.15",
    "org.scalatestplus" %% "mockito-4-6"         % "3.2.15.0"
  ).map(_ % "test, it")
}
