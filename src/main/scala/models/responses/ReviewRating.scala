package models.responses

import io.circe.{Decoder, Encoder, HCursor, Json}

case class ReviewRating(asin: String, averageRating: BigDecimal)

object ReviewRating:
  implicit val encoder: Encoder[ReviewRating] = new Encoder[ReviewRating] {
    final def apply(bestRated: ReviewRating): Json = Json.obj(
      ("asin", Json.fromString(bestRated.asin)),
      ("average_rating", Json.fromBigDecimal(bestRated.averageRating))
    )
  }

  implicit val decoder: Decoder[ReviewRating] =
    new Decoder[ReviewRating] {
      final def apply(c: HCursor): Decoder.Result[ReviewRating] =
        for {
          asin          <- c.downField("asin").as[String]
          averageRating <- c.downField("average_rating").as[BigDecimal]
        } yield {
          ReviewRating(asin, averageRating)
        }
    }
