package amazonreviewapi

import cats.effect.{ExitCode, IO, IOApp}

object Main extends IOApp.Simple:

  // TODO: Use secure data exchange (because there are peoples names in the data etc) https: //http4s.org/v0.23/docs/hsts.html

  val run = Server.run[IO]
