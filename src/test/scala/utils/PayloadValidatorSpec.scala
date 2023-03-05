package utils

import models.errors.ValidationError
import models.requests.BestReviewRequest
import models.responses.ReviewRating
import munit.CatsEffectSuite
import utils.PayloadValidator

import java.nio.file.Paths

class PayloadValidatorSpec extends CatsEffectSuite:

  for (
    invalidDate <- Seq("01/02/2020", "01.13.2000", "40.01.2020", "xx.xx.xxx")
  ) {
    test(
      s"Returns a ValidationError when given an invalid start or end date $invalidDate"
    ) {
      assertIO(
        {
          val req = BestReviewRequest(invalidDate, "01.01.2000", 1, 1)
          PayloadValidator.validateBestReviewRequest(req).value
        },
        Left(ValidationError(s"Invalid date: $invalidDate"))
      )

      assertIO(
        {
          val req = BestReviewRequest("01.01.2000", invalidDate, 1, 1)
          PayloadValidator.validateBestReviewRequest(req).value
        },
        Left(ValidationError(s"Invalid date: $invalidDate"))
      )
    }
  }

  test("Returns a ValidationError when the end date is prior the start date") {
    assertIO(
      {
        val req = BestReviewRequest("01.01.2010", "01.01.2000", 1, 1)
        PayloadValidator.validateBestReviewRequest(req).value
      },
      Left(ValidationError("End date should be before start date"))
    )
  }

  test(
    "Returns a ValidationError when the limit or min_number_reviews is less than 0"
  ) {
    assertIO(
      {
        val req = BestReviewRequest("01.01.2010", "01.01.2020", -1, 1)
        PayloadValidator.validateBestReviewRequest(req).value
      },
      Left(
        ValidationError(s"Expected positive numerical value in limit. Found -1")
      )
    )

    assertIO(
      {
        val req = BestReviewRequest("01.01.2010", "01.01.2020", 1, -1)
        PayloadValidator.validateBestReviewRequest(req).value
      },
      Left(
        ValidationError(
          s"Expected positive numerical value in min_number_reviews. Found -1"
        )
      )
    )
  }

  test("Returns Unit given all fields are valid") {
    assertIO(
      {
        val req = BestReviewRequest("01.01.2000", "01.01.2010", 1, 1)
        PayloadValidator.validateBestReviewRequest(req).value
      },
      Right(())
    )
  }
