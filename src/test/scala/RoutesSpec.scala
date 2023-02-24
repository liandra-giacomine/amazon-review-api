package amazonreviewapi

import cats.effect.IO
import models.Review
import org.http4s.*
import org.http4s.implicits.*
import munit.CatsEffectSuite

class RoutesSpec extends CatsEffectSuite:

  test("GET /amazon/best-review returns status code 200") {
    assertIO(getBestReview.map(_.status), Status.Ok)
  }

  test("HelloWorld returns hello world message") {
    assertIO(
      getBestReview.flatMap(_.as[String]),
      // [{"asin":"B000JQ0JNS","average_rating":4.5},{"asin":"B000NI7RW8","average_rating":3.666666666666666666666666666666667}]
      "[{\"asin\":\"B000JQ0JNS\",\"average_rating\":4.5},{\"asin\":\"B000NI7RW8\",\"average_rating\":3.666666666666666666666666666666667}]"
      // "[{\"asin\":\"B000JQ0JNS\",\"average_rating\":4.5},{\"asin\":\"B000NI7RW8\",\"average_rating\":3.666666666666666666666666666666667}]"
    )
  }

  private[this] val getBestReview: IO[Response[IO]] =
    Routes
      .reviewRoutes[IO]
      .orNotFound
      .run(
        Request(method = Method.GET, uri = uri"/amazon/best-review")
      )
