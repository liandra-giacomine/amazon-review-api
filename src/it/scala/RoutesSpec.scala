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
import utils.RequestCache

class RoutesSpec extends CatsEffectSuite:

  val mockRequestCache = mock[RequestCache]
  val routes           = Routes(mockRequestCache)

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

  val validPayload = Json
    .fromFields(
      List(
        ("start", Json.fromString("01.01.2010")),
        ("end", Json.fromString("01.01.2020")),
        ("limit", Json.fromInt(1)),
        ("min_number_reviews", Json.fromInt(1))
      )
    )
    .toString

  private def getBestReview(
      payload: String = validPayload
  ): IO[Response[IO]] =
    routes.reviewRoutes.orNotFound
      .run(
        Request(
          method = Method.POST,
          uri = uri"/amazon/best-review"
        ).withEntity(payload)
      )

  val validPayloadReq = getBestReview()

  test(
    "POST /amazon/best-review returns status code Ok given a successful response from the persistence service"
  ) {
    when(mockRequestCache.get(any()))
      .thenReturn(IO(Right(reviews)))

    assertIO(validPayloadReq.map(_.status), Status.Ok)
  }

  test(
    "POST /amazon/best-review returns array of asin and average_rating objects"
  ) {

    when(mockRequestCache.get(any()))
      .thenReturn(IO(Right(reviews)))

    assertIO(
      validPayloadReq.flatMap(r => r.as[Seq[ReviewRating]]),
      Seq(ReviewRating("B000JQ0JNS", 4.5))
    )
  }

  test(
    "POST /amazon/best-review returns bad request when it receives a payload it cannot parse"
  ) {
    when(mockRequestCache.get(any()))
      .thenReturn(IO(Left(PersistenceError("error"))))

    val invalidJsonReq = getBestReview("Invalid json")

    assertIO(invalidJsonReq.map(_.status), Status.BadRequest)

    assertIO(
      invalidJsonReq.flatMap(_.as[String]),
      "Malformed message body: Invalid JSON"
    )
  }

  test(
    "POST /amazon/best-review returns bad request when the payload fails validation"
  ) {

    val invalidDate = "123.01.2020"

    val payloadWithInvalidDate = Json
      .fromFields(
        List(
          ("start", Json.fromString(invalidDate)),
          ("end", Json.fromString("01.01.2020")),
          ("limit", Json.fromInt(1)),
          ("min_number_reviews", Json.fromInt(1))
        )
      )
      .toString

    val invalidPayloadReq = getBestReview(payloadWithInvalidDate)

    when(mockRequestCache.get(any()))
      .thenReturn(IO(Left(PersistenceError("error"))))

    assertIO(
      invalidPayloadReq.map(_.status),
      Status.BadRequest
    )

    assertIO(
      invalidPayloadReq.flatMap(_.as[String]),
      s"Invalid date: $invalidDate"
    )
  }

  test(
    "POST /amazon/best-review returns internal server error given a failure response from the persistence service"
  ) {
    when(mockRequestCache.get(any()))
      .thenReturn(IO(Left(PersistenceError("error"))))

    assertIO(validPayloadReq.map(_.status), Status.InternalServerError)
  }
