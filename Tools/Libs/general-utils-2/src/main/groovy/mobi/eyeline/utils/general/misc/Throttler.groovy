package mobi.eyeline.utils.general.misc

import com.google.common.base.MoreObjects
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import groovy.transform.CompileStatic
import mobi.eyeline.utils.general.caches.ReloadableCacheBase
import mobi.eyeline.utils.general.types.Rate

import java.util.concurrent.Callable

/**
 * Implements a throttling check, e.g. helps to ensure that rate of any distinct consumer
 * never exceeds the specified limit.
 *
 * <h1>Example:
 * <pre>
 *   // Maximal of 10 calls per minute.
 *   Throttler throttler = new Throttler( rateOf(10, duration('1m')) )
 *
 *   def callIfAllowed(String consumerId, Closure action) {
 *     if (throttler.isThrottled(consumerId)) {
 *       throw new Exception("Rate limit exceeded, try again later")
 *     }
 *
 *     action.call()
 *   }
 * </pre>
 */
@CompileStatic
class Throttler extends ReloadableCacheBase<String, TokenBucket> {

  private final Callable<TokenBucket> loader = { new TokenBucket(getRate()) }

  private Rate rate

  Throttler(Rate initialRate = null) {
    if (initialRate) {
      setRate initialRate
    }
  }

  @Override
  protected Cache<String, TokenBucket> buildCache() {
    !rate ? null : CacheBuilder
        .newBuilder()
        .expireAfterAccess(2 * rate.interval.value, rate.interval.unit)
        .build()
  }

  boolean isThrottled(String id) {
    withCache { cache ->
      final bucket = cache?.get(id, loader) as TokenBucket
      return bucket?.isThrottled()
    }
  }

  Rate getRate() {
    rate
  }

  Throttler setRate(Rate rate) {
    final changed = (this.rate?.interval != rate?.interval)

    this.rate = rate

    if (changed) {
      reload()
    }

    this
  }

  @Override
  String toString() {
    MoreObjects
        .toStringHelper(this)
        .add('rate', rate)
        .toString()
  }
}