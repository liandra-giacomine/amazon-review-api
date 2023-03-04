package amazonreviewapi

import cats.data.EitherT
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
import connectors.PersistenceConnector
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.implicits.*
import io.circe.syntax.*
import org.http4s.circe.*
import io.circe.parser.*
import models.errors.ValidationError
import models.responses.ReviewRating
import models.requests.BestReviewRequest
import org.http4s.client.{Client, JavaNetClientBuilder}
import org.http4s.ember.client.EmberClientBuilder
import utils.PayloadValidator

import concurrent.duration.DurationInt
import java.nio.file.Paths
import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

class Routes(persistenceConnector: PersistenceConnector):

  implicit val decoder: EntityDecoder[IO, BestReviewRequest] =
    jsonOf[IO, BestReviewRequest]

  implicit val encoder: EntityDecoder[IO, Seq[ReviewRating]] =
    jsonOf[IO, Seq[ReviewRating]]

  val reviewRoutes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case req @ POST -> Root / "amazon" / "best-review" =>
      import org.http4s.dsl.io.*
      implicit val runtime = cats.effect.unsafe.IORuntime.global

      req
        .as[BestReviewRequest]
        .attempt
        .map {
          case Left(thr) => BadRequest(thr.getMessage)
          case Right(bestReviewReq) =>
            PayloadValidator
              .validateBestReviewRequest(bestReviewReq)
              .value
              .unsafeRunSync() match {
              case Left(validationError) => BadRequest(validationError.message)
              case Right(_) =>
                persistenceConnector.findBestReviews(bestReviewReq) match {
                  case Left(_)  => InternalServerError()
                  case Right(r) => Ok(r.asJson)
                }
            }
        }
        .unsafeRunSync()

  }
