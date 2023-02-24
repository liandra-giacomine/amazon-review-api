package models

import io.circe.*
import io.circe.generic.semiauto.*

import java.time.Instant
import java.util.Date

final case class Review(
    asin: String,
    helpful: (Int, Int),
    overall: Double,
    reviewText: String,
    reviewerID: String,
    reviewerName: String,
    summary: String,
    unixReviewTime: UnixTimeStamp
)

object Review:
  implicit val decoder: Decoder[Review] = deriveDecoder[Review]
  implicit val encoder: Encoder[Review] = deriveEncoder[Review]
