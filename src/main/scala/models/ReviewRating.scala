package models

import io.circe.{Encoder, Json}

case class ReviewRating(asin: String, averageRating: BigDecimal)

object ReviewRating:
  implicit val encodeFoo: Encoder[ReviewRating] = new Encoder[ReviewRating] {
    final def apply(bestRated: ReviewRating): Json = Json.obj(
      ("asin", Json.fromString(bestRated.asin)),
      ("average_rating", Json.fromBigDecimal(bestRated.averageRating))
    )
  }
