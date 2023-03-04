package connectors

import cats.data.EitherT
import cats.effect.{IO, Resource}
import cats.effect.unsafe.IORuntime
import io.circe.generic.auto.*
import models.errors.PersistenceError
import models.requests.BestReviewRequest
import models.responses.ReviewRating
import org.http4s.Uri.Path
import org.http4s.Uri.Path.Segment
import org.http4s.circe.*
import org.http4s.client.Client
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.headers.Accept
import org.http4s.implicits.uri
import org.http4s.{
  EntityDecoder,
  EntityEncoder,
  Headers,
  MediaType,
  Method,
  Request,
  Uri,
  UrlForm
}
import org.http4s.client.middleware.Logger

class PersistenceConnector(client: Resource[IO, Client[IO]]):

  implicit val encoder: EntityEncoder[IO, BestReviewRequest] =
    jsonEncoderOf[IO, BestReviewRequest]

  implicit val decoder: EntityDecoder[IO, Seq[ReviewRating]] =
    jsonOf[IO, Seq[ReviewRating]]

  private def postRequest(body: BestReviewRequest) = Request[IO](
    method = Method.POST,
    uri = uri"http://localhost:8081/reviews/best"
  ).withEntity[BestReviewRequest](body)

  def findBestReviews(
      body: BestReviewRequest
  )(implicit
      runtime: IORuntime
  ): Either[PersistenceError, Seq[ReviewRating]] =
    client
      .use(client =>
        Logger(logBody = true, logHeaders = true)(client)
          .run(postRequest(body))
          .use(resp => resp.as[Seq[ReviewRating]])
      )
      .attempt
      .map {
        case Left(e)        => Left(PersistenceError(e.getMessage))
        case Right(reviews) => Right(reviews)
      }
      .unsafeRunSync()
