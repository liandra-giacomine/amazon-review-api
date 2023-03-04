package amazonreviewapi

import cats.effect.unsafe.IORuntime
import cats.effect.{IO, Resource}
import connectors.PersistenceConnector
import models.errors.{PersistenceError, ValidationError}
import models.requests.BestReviewRequest
import models.responses.ReviewRating
import munit.CatsEffectSuite
import org.http4s.client.Client
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalatest.matchers.must.Matchers.mustBe
import org.scalatestplus.mockito.MockitoSugar.mock
import utils.PayloadValidator

class PersistenceConnectorSpec extends CatsEffectSuite:

  val mockClient           = mock[Resource[IO, Client[IO]]]
  val persistenceConnector = PersistenceConnector(mockClient)
  val bestReviewRequest    = BestReviewRequest("01.01.2000", "01.01.2010", 1, 1)
  implicit val runtime: IORuntime = cats.effect.unsafe.IORuntime.global

  test(
    "Returns a sequence of Review results when the client parses the result as such"
  ) {
    val clientResponse = Seq(ReviewRating("asin", 1))

    when(mockClient.use(any())(any())).thenReturn(IO(clientResponse))

    persistenceConnector.findBestReviews(bestReviewRequest) mustBe Right(
      clientResponse
    )
  }

  test(
    "Returns a PersistenceError when the client throws an exception"
  ) {
    val errorMessage = "error"

    when(mockClient.use(any())(any()))
      .thenReturn(IO(throw new Exception(errorMessage)))

    persistenceConnector.findBestReviews(bestReviewRequest) mustBe Left(
      PersistenceError(errorMessage)
    )
  }
