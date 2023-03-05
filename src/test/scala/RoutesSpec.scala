package amazonreviewapi

import cats.effect.IO
import connectors.PersistenceConnector
import io.circe.Json
import models.errors.PersistenceError
import models.requests.BestReviewRequest
import models.responses.ReviewRating
import org.http4s.*
import org.http4s.implicits.*
import munit.CatsEffectSuite
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import org.mockito.ArgumentMatchers.any

class RoutesSpec extends CatsEffectSuite:

  val mockPersistenceConnector = mock[PersistenceConnector]
  val routes                   = Routes(mockPersistenceConnector)

  implicit val encoder: EntityEncoder[IO, BestReviewRequest] =
    jsonEncoderOf[IO, BestReviewRequest]

  implicit val decoderReviewRequest: EntityDecoder[IO, BestReviewRequest] =
    jsonOf[IO, BestReviewRequest]

  implicit val encoderReviewRating: EntityEncoder[IO, Seq[ReviewRating]] =
    jsonEncoderOf[IO, Seq[ReviewRating]]

  implicit val decoder: EntityDecoder[IO, Seq[ReviewRating]] =
    jsonOf[IO, Seq[ReviewRating]]

  val bestReviewRequest = BestReviewRequest("01.01.2000", "01.01.2010", 1, 1)
  val reviews           = Seq(ReviewRating("B000JQ0JNS", 4.5))

  val json = Json
    .fromFields(
      List(
        ("start", Json.fromString("01.01.2010")),
        ("end", Json.fromString("01.01.2020")),
        ("limit", Json.fromInt(1)),
        ("min_number_reviews", Json.fromInt(1))
      )
    )
    .toString

  private[this] val getBestReview: IO[Response[IO]] =
    routes.reviewRoutes.orNotFound
      .run(
        Request(
          method = Method.POST,
          uri = uri"/amazon/best-review"
        ).withEntity(json)
      )

  test(
    "POST /amazon/best-review returns status code Ok given a successful response from the persistence service"
  ) {
    when(mockPersistenceConnector.findBestReviews(any()))
      .thenReturn(IO(Right(reviews)))

    assertIO(getBestReview.map(_.status), Status.Ok)
  }

  test(
    "POST /amazon/best-review returns array of asin and average_rating objects"
  ) {

    when(mockPersistenceConnector.findBestReviews(any()))
      .thenReturn(IO(Right(reviews)))

    assertIO(
      getBestReview.flatMap(r => r.as[Seq[ReviewRating]]),
      Seq(ReviewRating("B000JQ0JNS", 4.5))
    )
  }

  test(
    "POST /amazon/best-review returns internal server error given a failure response from the persistence service"
  ) {
    when(mockPersistenceConnector.findBestReviews(any()))
      .thenReturn(IO(Left(PersistenceError("error"))))

    assertIO(getBestReview.map(_.status), Status.InternalServerError)
  }
