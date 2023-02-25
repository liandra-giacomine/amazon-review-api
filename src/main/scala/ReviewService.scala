package amazonreviewapi

import java.nio.file.Paths
import java.io.{File, FileInputStream}
import fs2.io.file.{Files, Path}
import fs2.{Pipe, Stream, text}
import cats.effect.{IO, Resource, Sync}
import cats.implicits.*
import io.circe.parser.*
import models.Review
object ReviewService:

  // TODO: Handle fromNIOPath, it may throw an exception if the file doesn't exist
  // TODO: Need to handle json conversion and deserialisation errors

  private def isWithinGivenTimeRange(
      review: Review,
      fromTimeStamp: Long,
      toTimeStamp: Long
  ): Boolean =
    (review.unixReviewTime >= fromTimeStamp) && (review.unixReviewTime <= toTimeStamp)

  private def collectReviews(
      fromTimeStamp: Long,
      toTimeStamp: Long
  ): Pipe[IO, Byte, Review] = { src =>
    src
      .through(text.utf8.decode)
      .through(text.lines)
      .map(line => parse(line))
      .map {
        case Right(json) =>
          json.as[Review].toOption // assumption - ignore reviews that are not in the expected JSON format
        case Left(_) => None
      }
      .filter(reviewOption =>
        reviewOption.isDefined && isWithinGivenTimeRange(
          reviewOption.get,
          fromTimeStamp,
          toTimeStamp
        )
      )
      .collect { case Some(review) => review }
  // .take(2)
  }

  def getBestReviews(
      readFrom: String,
      fromTimeStamp: Long,
      toTimeStamp: Long
  ): IO[List[Review]] = {
    val fs2Path = Path.fromNioPath(
      java.nio.file.Paths.get(readFrom)
    )
    val source: Stream[IO, Byte] = Files[IO].readAll(fs2Path)
    source
      .through(collectReviews(fromTimeStamp, toTimeStamp))
      .compile
      .toList
  }
//    .compile.toList
