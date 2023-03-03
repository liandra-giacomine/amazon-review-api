package amazonreviewapi

import cats.effect.{Async, ExitCode, IO}
import cats.syntax.all.*
import com.comcast.ip4s.*
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits.*
import org.http4s.server.middleware.Logger

object Server:

  def run: IO[Unit] =
    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(Routes.reviewRoutes.orNotFound)
      .build
      .use(_ => IO.never)
      .as(())

//  def run[F[_]: Async]: F[Nothing] = {
//    for {
//      client <- EmberClientBuilder.default[F].build
//
//      // Combine Service Routes into an HttpApp.
//      // Can also be done via a Router if you
//      // want to extract a segments not checked
//      // in the underlying routes.
//      httpApp = (
//        Routes.reviewRoutes[F]
//      ).orNotFound
//
//      // With Middlewares in place
//      finalHttpApp = Logger.httpApp(true, true)(Routes.reviewRoutes)
//
//      _ <-
//        EmberServerBuilder
//          .default[F]
//          .withHost(ipv4"0.0.0.0")
//          .withPort(port"8080")
//          .withHttpApp(finalHttpApp)
//          .build
//    } yield ()
//  }.useForever
