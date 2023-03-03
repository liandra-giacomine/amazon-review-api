package utils

import cats.data.EitherT
import cats.effect.IO
import models.errors.ValidationError
import models.requests.BestReviewRequest
import org.http4s.Request
import utils.PayloadValidator.{dateFormat, validateDate}

import scala.util.Try

object PayloadValidator:

  private val dateFormat = new java.text.SimpleDateFormat("dd.MM.yyyy")

  def validateBestReviewRequest(
      r: BestReviewRequest
  ): EitherT[IO, ValidationError, Unit] =
    for {
      _          <- validateDate(r.startDate)
      _          <- validateDate(r.endDate)
      _          <- validatePositiveInt(r.limit, "limit")
      minReviews <- validatePositiveInt(r.minReviews, "min_number_reviews")
    } yield minReviews

  private def validatePositiveInt(value: Int, field: String) =
    EitherT {
      IO {
        if (value >= 0) Right(())
        else
          Left(
            ValidationError(
              s"Expected positive numerical value in $field. Found $value"
            )
          )
      }
    }

  private def validateDate(date: String) =
    EitherT {
      IO {
        dateFormat.setLenient(false)
        Right(dateFormat.parse(date))
      }.handleError { _ =>
        Left(ValidationError(s"Invalid date: $date"))
      }
    }
