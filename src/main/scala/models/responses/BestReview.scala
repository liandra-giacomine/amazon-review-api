package models.responses

import io.circe.*
import io.circe.generic.semiauto.*

case class BestReview(asin: String, averageRating: BigDecimal)

object BestReview:
  implicit val encodeFoo: Encoder[BestReview] = new Encoder[BestReview] {
    final def apply(bestRated: BestReview): Json = Json.obj(
      ("asin", Json.fromString(bestRated.asin)),
      ("average_rating", Json.fromBigDecimal(bestRated.averageRating))
    )
  }
