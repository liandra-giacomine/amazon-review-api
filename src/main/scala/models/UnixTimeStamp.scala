package models

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

case class UnixTimeStamp(value: Long)

object UnixTimeStamp:
  implicit val decoder: Decoder[UnixTimeStamp] = deriveDecoder[UnixTimeStamp]
  implicit val encoder: Encoder[UnixTimeStamp] = deriveEncoder[UnixTimeStamp]
