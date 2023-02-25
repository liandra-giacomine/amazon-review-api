package amazonreviewapi

import cats.effect.{IO, Resource, Sync}
import cats.implicits.*
import fs2.{Pipe, Pure, Stream, text}
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

import java.io.{File, FileInputStream}
import fs2.io.file.{Files, Path}
import org.http4s.server.*
import org.http4s.dsl.io.Ok
import cats.effect.*
import cats.syntax.all.*
import models.responses.BestReview
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.implicits.*
import io.circe.syntax.*
import org.http4s.circe.*
import io.circe.parser.*
import models.{Review, UnixTimeStamp}
import concurrent.duration.DurationInt

import java.nio.file.Paths
import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

object Routes:
//  def reviewRoutes: HttpRoutes[IO] =
//    val dsl = new Http4sDsl[IO] {}
  def reviewRoutes[F[_]: Sync]: HttpRoutes[F] =
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] { case GET -> Root / "amazon" / "best-review" =>
      val listReview = List(
        Review("a", (1, 2), 3.0, "b", "c", "d", "e", UnixTimeStamp(122342))
      )
      val runtime = cats.effect.unsafe.IORuntime.global
      (for {
        reviews <- collectReviews(
          "/Users/taybeers/Documents/development/reviews.json",
          UnixTimeStamp(1262304000),
          UnixTimeStamp(1609372800)
        ).compile.toList
      } yield Ok(reviews.asJson)).unsafeRunSync()(runtime)

//      Ok(x.asJson)
//      val x =
//        List(
//          BestReview("B000JQ0JNS", 4.5),
//          BestReview(
//            "B000NI7RW8",
//            BigDecimal("3.666666666666666666666666666666667")
//          )
//        ).asJson

//      val x = for {
//        reviews <- collectReviews(
//          "path",
//          UnixTimeStamp(1262304000),
//          UnixTimeStamp(1609372800)
//        )
//      } yield Ok(reviews.asJson)

    // Ok(BestRatedResponse(List(BestRated("abc", 1.0))).asJson)
//      for {
//        // joke <- readAndWriteFile("fromPath", "toPath")
//        joke <- J.get
//        resp <- Ok(joke)
//      } yield resp
    }

//  val converter = Files[IO]
//    .readAll(
//      Path.fromNioPath(
//        java.nio.file.Paths.get("filepath")
//      )
//    )
//    .through(text.utf8Decode)
//    .through(text.lines)
//    .filter(line => !line.trim.isEmpty && !line.startsWith("//"))
//    .map(line => (line.toDouble * 2).toString)
//    .take(3)
//
//  val program6 = converter.compile.drain

  def collectReviews(
      readFrom: String,
      fromTimeStamp: UnixTimeStamp,
      toTimeStamp: UnixTimeStamp
  ): Stream[IO, Review] =
    val fs2Path = Path.fromNioPath(
      java.nio.file.Paths.get(readFrom)
    ) // TODO: Handle this, it may throw an exception
    //  val source: Stream[IO, Byte] = Files[IO].readAll(fs2Path)
//    val pipe: Pipe[Pure, Byte, Review] = src =>
//      src
    Files[IO]
      .readAll(fs2Path)
      .through(text.utf8.decode)
      .through(text.lines)
      .map(line => parse(line))
      .map {
        case Right(json) => json.as[Review].toOption
        case Left(_)     => None
      }
//      .filter(reviewOption =>
//        reviewOption.isDefined && isWithinGivenTimeRange(
//          reviewOption.get,
//          fromTimeStamp,
//          toTimeStamp
//        )
//      )
      .collect { case Some(review) => review }
      .take(2)

  def run(
      readFrom: String,
      fromTimeStamp: UnixTimeStamp,
      toTimeStamp: UnixTimeStamp
  ): IO[Unit] =
    collectReviews(readFrom, fromTimeStamp, toTimeStamp).compile.drain

////        .flatMap(line =>
////          Stream.apply(line.asJson: _*)
////        ) // replace this with encoder
//        .fold(Map.empty[String, Int]) { (count, word) =>
//          count + (word -> (count.getOrElse(word, 0) + 1))
//        }
//        .map(_.foldLeft("") { case (accumulator, (word, count)) =>
//          accumulator + s"$word = $count\n"
//        })
//        .through(text.utf8.encode)
//    val sink: Pipe[IO, Byte, Unit] = Files[IO].writeAll(Path(writeTo))
//    source
//      .through(pipe)
//      .through(sink)

  private def isWithinGivenTimeRange(
      review: Review,
      fromTimeStamp: UnixTimeStamp,
      toTimeStamp: UnixTimeStamp
  ): Boolean =
    (review.unixReviewTime.value >= fromTimeStamp.value) && (review.unixReviewTime.value <= toTimeStamp.value)
