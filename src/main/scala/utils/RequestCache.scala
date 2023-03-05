package utils

import cats.effect.unsafe.IORuntime
import com.google.common.cache.{CacheBuilder, CacheLoader}
import com.google.common.collect.Multiset.Entry
import connectors.PersistenceConnector
import models.errors.PersistenceError
import models.requests.BestReviewRequest
import models.responses.ReviewRating

import java.util.concurrent.{Callable, TimeUnit}

class RequestCache(persistenceConnector: PersistenceConnector)(implicit
    runtime: IORuntime
) {

  private val reviewCacheLoader =
    new CacheLoader[BestReviewRequest, Either[PersistenceError, Seq[
      ReviewRating
    ]]] {
      override def load(key: BestReviewRequest): Either[PersistenceError, Seq[
        ReviewRating
      ]] = persistenceConnector.findBestReviews(key).unsafeRunSync()
    }

  val getCache = CacheBuilder
    .newBuilder()
    .maximumSize(10000L)
    .expireAfterAccess(10L, TimeUnit.MINUTES)
    .build[BestReviewRequest, Either[PersistenceError, Seq[ReviewRating]]](
      reviewCacheLoader
    )

}

//class BestReviewCacheLoader(persistenceConnector: PersistenceConnector)
//    extends CacheLoader[BestReviewRequest, Either[PersistenceError, Seq[
//      ReviewRating
//    ]]] {
//  override def load(key: BestReviewRequest): Either[PersistenceError, Seq[
//    ReviewRating
//  ]] = persistenceConnector.findBestReviews(key).unsafeRunSync()
//}
