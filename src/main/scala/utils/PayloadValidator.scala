package utils

import cats.data.EitherT
import cats.effect.IO
import models.errors.ValidationError
import models.requests.BestReviewRequest
import org.http4s.Request
import utils.PayloadValidator.{dateFormat, validateDate}

import java.util.Date
import scala.util.Try

object PayloadValidator:

  private val dateFormat = {
    val formatter = new java.text.SimpleDateFormat("dd.MM.yyyy")
    formatter.setLenient(false)
    formatter
  }

  def validateBestReviewRequest(
      r: BestReviewRequest
  ): EitherT[IO, ValidationError, Unit] =
    for {
      start      <- validateDate(r.startDate)
      end        <- validateDate(r.endDate)
      _          <- checkDateOrder(start, end)
      _          <- validatePositiveInt(r.limit, "limit")
      minReviews <- validatePositiveInt(r.minReviews, "min_number_reviews")
    } yield minReviews

  private def checkDateOrder(start: Date, end: Date) = {
    EitherT {
      IO {
        if (end.before(start))
          Left(ValidationError("End date should be before start date"))
        else Right(())
      }
    }
  }

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
        Right(dateFormat.parse(date))
      }.handleError { _ =>
        Left(ValidationError(s"Invalid date: $date"))
      }
    }
