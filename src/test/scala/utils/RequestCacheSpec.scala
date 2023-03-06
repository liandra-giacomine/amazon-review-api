package utils

import cats.effect.IO
import connectors.PersistenceConnector
import models.errors.PersistenceError
import models.requests.BestReviewRequest
import models.responses.ReviewRating
import munit.CatsEffectSuite
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock

class RequestCacheSpec extends CatsEffectSuite:

  val mockPersistenceConnector = mock[PersistenceConnector]
  val cache                    = new RequestCache(mockPersistenceConnector)

  val req = BestReviewRequest("01.01.2000", "01.01.2010", 1, 1)

  val reviews = Seq(ReviewRating("B000JQ0JNS", 3.0))

  test("Returns the sequence of review returned by the persistence connector") {
    when(mockPersistenceConnector.findBestReviews(any()))
      .thenReturn(IO(Right(reviews)))

    assertIO(
      {
        cache.get(req)
      },
      Right(reviews)
    )
  }

  test("Retrieves cached result when given the same input") {
    assertIO(
      {
        cache.get(req)
      },
      Right(reviews)
    )
  }

  test("Returns the persistence error returned by the persistence connector") {
    when(mockPersistenceConnector.findBestReviews(any()))
      .thenReturn(IO(Left(PersistenceError("test"))))

    val req = BestReviewRequest("01.01.2020", "01.01.2022", 1, 1)

    assertIO(
      {
        cache.get(req)
      },
      Left(PersistenceError("test"))
    )
  }
