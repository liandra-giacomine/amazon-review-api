package utils

import cats.effect.IO
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

  private val loader =
    new CacheLoader[BestReviewRequest, Either[PersistenceError, Seq[
      ReviewRating
    ]]] {
      override def load(key: BestReviewRequest): Either[PersistenceError, Seq[
        ReviewRating
      ]] = persistenceConnector.findBestReviews(key).unsafeRunSync()
    }

  private val cache = CacheBuilder
    .newBuilder()
    .maximumSize(10000L)
    .expireAfterAccess(10L, TimeUnit.MINUTES)
    .build[BestReviewRequest, Either[PersistenceError, Seq[ReviewRating]]](
      loader
    )

  def get(key: BestReviewRequest) = IO(cache.get(key))

}

//class BestReviewCacheLoader(persistenceConnector: PersistenceConnector)
//    extends CacheLoader[BestReviewRequest, Either[PersistenceError, Seq[
//      ReviewRating
//    ]]] {
//  override def load(key: BestReviewRequest): Either[PersistenceError, Seq[
//    ReviewRating
//  ]] = persistenceConnector.findBestReviews(key).unsafeRunSync()
//}
