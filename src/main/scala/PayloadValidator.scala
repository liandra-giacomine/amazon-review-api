package amazonreviewapi

import PayloadValidator.{dateFormat, validateDate}
import cats.data.EitherT
import cats.effect.IO
import models.{BestReviewRequest, ValidationError}
import org.http4s.Request

import scala.util.Try

object PayloadValidator:

  private val dateFormat = IO {
    val dateFormat = new java.text.SimpleDateFormat("dd.MM.yyyy")
    dateFormat.setLenient(false)
    dateFormat
  }

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
      dateFormat.map(d => Right(d.parse(date))).handleError { thr =>
        Left(ValidationError(s"Invalid date: $date"))
      }
    }
