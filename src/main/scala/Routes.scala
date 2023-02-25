package amazonreviewapi

import cats.effect.{IO, Resource, Sync}
import cats.implicits.*
import fs2.{Pipe, Pure, Stream, text}
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

import java.io.{File, FileInputStream}
import fs2.io.file.{Files, Path}
import org.http4s.server.*
import org.http4s.dsl.io.Ok
import cats.effect.*
import cats.syntax.all.*
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.implicits.*
import io.circe.syntax.*
import org.http4s.circe.*
import io.circe.parser.*
import models.{ReviewSummary, ReviewRating}
import concurrent.duration.DurationInt

import java.nio.file.Paths
import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

object Routes:

// TODO: Throw a bad request if toTimeStamp is more than fromTimeStamp?
  def reviewRoutes[F[_]: Sync]: HttpRoutes[F] =
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] { case GET -> Root / "amazon" / "best-review" =>
      val runtime = cats.effect.unsafe.IORuntime.global
      (for {
        reviews <- ReviewService
          .getBestReviews(
            Paths
              .get("")
              .toAbsolutePath
              .toString + "/src/test/scala/resources/reviews.json",
            1262304000,
            1609372800,
            2,
            2
          )
      } yield Ok(reviews.asJson)).unsafeRunSync()(runtime)
    }
