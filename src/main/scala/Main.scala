package amazonreviewapi

import cats.effect.{ExitCode, IO, IOApp}

object Main extends IOApp.Simple:
  val run = Server.run[IO]
