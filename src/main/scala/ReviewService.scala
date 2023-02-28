package amazonreviewapi

import java.nio.file.Paths
import java.io.{File, FileInputStream}
import fs2.io.file.{Files, Path}
import fs2.{Pipe, Stream, text}
import cats.effect.{IO, Resource, Sync}
import io.circe.parser.*
import models.{ReviewRating, ReviewSummary}

import scala.util.Sorting

object ReviewService:

  private def collectReviews(
      fromTimeStamp: Long,
      toTimeStamp: Long
  ): Pipe[IO, Byte, ReviewSummary] = {
    def isWithinGivenTimeRange(
        review: ReviewSummary
    ): Boolean =
      (review.unixReviewTime >= fromTimeStamp) && (review.unixReviewTime <= toTimeStamp)

    src =>
      src
        .through(text.utf8.decode)
        .through(text.lines)
        .map(line => parse(line))
        .map {
          case Right(json) =>
            json
              .as[ReviewSummary]
              .toOption // assumption - ignore reviews that are not in the expected JSON format
          case Left(_) => None
        }
        .filter(reviewOption =>
          reviewOption.isDefined && isWithinGivenTimeRange(
            reviewOption.get
          )
        )
        .collect { case Some(review) => review }
  }

  def getBestReviews(
      readFrom: String,
      fromTimeStamp: Long,
      toTimeStamp: Long,
      minReviews: Int,
      returnLimit: Int
  ): IO[List[ReviewRating]] = {
    def sortReviewRatingsAndTakeLimit(reviewRatings: List[ReviewRating]) =
      IO(
        reviewRatings
          .sortBy(_.averageRating)(Ordering.BigDecimal.reverse)
          .take(returnLimit)
      )

    val fs2Path = Path.fromNioPath(
      java.nio.file.Paths.get(readFrom)
    )

    Files[IO]
      .readAll(fs2Path)
      .through(collectReviews(fromTimeStamp, toTimeStamp))
      .compile
      .toList
      .flatMap { reviews =>
        convertToReviewRatingsAndFilter(reviews, minReviews)
      }
      .flatMap { reviewRatings =>
        sortReviewRatingsAndTakeLimit(reviewRatings)
      }
  }

  private def convertToReviewRatingsAndFilter(
      reviews: List[ReviewSummary],
      minReviews: Int
  ) = {
    def addIfHasEnoughReviews(
        asin: String,
        reviewList: List[ReviewSummary],
        minReviews: Int
    ) = {
      if (reviewList.length < minReviews)
        List.empty[ReviewRating]
      else
        List(
          ReviewRating(
            asin,
            reviewList
              .map(_.overall)
              .sum / reviewList.length
          )
        )
    }

    IO {
      reviews.groupBy(_.asin).foldLeft(List.empty[ReviewRating]) {
        (bestReviewList, asinAndReviewList) =>
          bestReviewList ++ addIfHasEnoughReviews(
            asinAndReviewList._1,
            asinAndReviewList._2,
            minReviews
          )
      }
    }
  }
