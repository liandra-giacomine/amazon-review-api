package models.requests

import io.circe.{Decoder, HCursor}
import models.UnixTimeStamp

import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.Date

//is it a wise decision to be converting it to unixReviewTime
case class BestRatedRequest(
    start: UnixTimeStamp,
    end: UnixTimeStamp,
    limit: Int,
    minReviews: Int
)

object BestRatedRequest:
  implicit val decodeFoo: Decoder[BestRatedRequest] =
    new Decoder[BestRatedRequest] {
      final def apply(c: HCursor): Decoder.Result[BestRatedRequest] =
        for {
          startStr   <- c.downField("start").as[String]
          endStr     <- c.downField("end").as[String]
          limit      <- c.downField("limit").as[Int]
          minReviews <- c.downField("min_number_reviews").as[Int]
          unixStartDate = convertToUnixTimeStamp(startStr)
          unixEndDate   = convertToUnixTimeStamp(endStr)
        } yield {
          new BestRatedRequest(unixStartDate, unixEndDate, limit, minReviews)
        }
    }

  private val dateFormat = new java.text.SimpleDateFormat("dd.MM.yyyy")
  private def convertToUnixTimeStamp(dateString: String) =
    val unixTimeInMilliseconds =
      dateFormat.parse(dateString).getTime // TODO: Convert to seconds
    UnixTimeStamp(unixTimeInMilliseconds)

//{
//  "start": "01.01.2010",
//  "end": "31.12.2020",
//  "limit": 2,
//  "min_number_reviews": 2
//}
