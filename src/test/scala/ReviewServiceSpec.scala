package amazonreviewapi

import models.ReviewSummary
import models.ReviewRating
import munit.CatsEffectSuite

import java.nio.file.Paths

//class ReviewServiceSpec extends CatsEffectSuite:

//  test("POST /amazon/best-review returns status code 200") {
//    assertIO(
//      ReviewService.getBestReviews(
//        Paths
//          .get("")
//          .toAbsolutePath
//          .toString + "/src/test/scala/resources/reviews.json",
//        1262304000,
//        1609372800,
//        2,
//        2
//      ),
//      List(
//        ReviewRating("B000JQ0JNS", 4.5),
//        ReviewRating(
//          "B000NI7RW8",
//          BigDecimal("3.666666666666666666666666666666667")
//        )
//      )
//    )
//  }
