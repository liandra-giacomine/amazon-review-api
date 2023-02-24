package amazonreviewapi

import cats.effect.{IO, Resource, Sync}
import cats.implicits.*
import fs2.Pipe
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import fs2.Stream

import java.io.{File, FileInputStream}
import fs2.{Stream, text}
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

object Routes:
  def reviewRoutes[F[_]: Sync]: HttpRoutes[F] =
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] { case GET -> Root / "amazon" / "best-review" =>
      val x =
        List(
          BestReview("B000JQ0JNS", 4.5),
          BestReview(
            "B000NI7RW8",
            BigDecimal("3.666666666666666666666666666666667")
          )
        ).asJson
      Ok(x)
    // Ok(BestRatedResponse(List(BestRated("abc", 1.0))).asJson)
//      for {
//        // joke <- readAndWriteFile("fromPath", "toPath")
//        joke <- J.get
//        resp <- Ok(joke)
//      } yield resp
    }

  def readAndWriteFile(readFrom: String, writeTo: String): Stream[IO, Unit] =
    val fs2Path = Path.fromNioPath(java.nio.file.Paths.get(readFrom))
    val source: Stream[IO, Byte] = Files[IO].readAll(fs2Path)
    val pipe: Pipe[IO, Byte, Byte] = src =>
      src
        .through(text.utf8.decode)
        .through(text.lines)
        .flatMap(line =>
          Stream.apply(line.split("\\W+"): _*)
        ) // replace this with encoder
        .fold(Map.empty[String, Int]) { (count, word) =>
          count + (word -> (count.getOrElse(word, 0) + 1))
        }
        .map(_.foldLeft("") { case (accumulator, (word, count)) =>
          accumulator + s"$word = $count\n"
        })
        .through(text.utf8.encode)
    val sink: Pipe[IO, Byte, Unit] = Files[IO].writeAll(Path(writeTo))
    source
      .through(pipe)
      .through(sink)

//  def helloWorldRoutes[F[_]: Sync](H: HelloWorld[F]): HttpRoutes[F] =
//    val dsl = new Http4sDsl[F]{}
//    import dsl._
//    HttpRoutes.of[F] {
//      case GET -> Root / "hello" / name =>
//        for {
//          greeting <- H.hello(HelloWorld.Name(name))
//          resp <- Ok(greeting)
//        } yield resp
//    }
