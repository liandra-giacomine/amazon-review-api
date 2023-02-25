package models

import io.circe.*
import io.circe.generic.semiauto.*

import java.time.Instant
import java.util.Date
final case class ReviewSummary(
    asin: String,
    helpful: (Int, Int),
    overall: BigDecimal,
    reviewText: String,
    reviewerID: String,
    reviewerName: String,
    summary: String,
    unixReviewTime: Long
)

object ReviewSummary:
  implicit val decoder: Decoder[ReviewSummary] = deriveDecoder[ReviewSummary]
  implicit val encoder: Encoder[ReviewSummary] = deriveEncoder[ReviewSummary]
