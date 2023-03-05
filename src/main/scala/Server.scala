package amazonreviewapi

import cats.effect.unsafe.IORuntime
import cats.effect.{Async, ExitCode, IO}
import cats.syntax.all.*
import com.comcast.ip4s.*
import connectors.PersistenceConnector
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits.*
import org.http4s.server.middleware.Logger
import utils.RequestCache

object Server:

  val persistenceConnector: PersistenceConnector =
    PersistenceConnector(EmberClientBuilder.default[IO].build)

  implicit val runtime: IORuntime = cats.effect.unsafe.IORuntime.global

  val requestCache   = new RequestCache(persistenceConnector)
  val routes: Routes = Routes(requestCache)

  val run: IO[Unit] = {

    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(routes.reviewRoutes.orNotFound)
      .build
      .use(_ => IO.never)
      .as(())
  }
