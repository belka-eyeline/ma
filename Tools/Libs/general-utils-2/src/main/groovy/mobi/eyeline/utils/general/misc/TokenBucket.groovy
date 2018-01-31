package mobi.eyeline.utils.general.misc

import com.google.common.base.MoreObjects
import groovy.transform.CompileStatic
import mobi.eyeline.utils.general.types.Duration
import mobi.eyeline.utils.general.types.Rate

/**
 * <a href="https://en.wikipedia.org/wiki/Token_bucket">Token_bucket</a> implementation.
 */
@CompileStatic
class TokenBucket {

  /** Events allowed per period */
  final int rate

  /** Period, millis */
  final double perMillis


  /** Messages */
  private double bucket = rate

  /** Last event timestamp */
  private long lastCheck = System.currentTimeMillis()

  TokenBucket(Rate rate) {
    this(rate.rate, rate.interval)
  }

  TokenBucket(int rate, Duration interval) {
    this.rate = rate
    this.perMillis = interval.millis
  }

  boolean isThrottled() {
    isThrottled new Date()
  }

  boolean isThrottled(Date now) {
    isThrottled now.time
  }

  boolean isThrottled(long now) {
    final delta = now - lastCheck
    lastCheck = now

    bucket += delta * (rate / perMillis)

    if (bucket > rate) {
      // Throttle
      bucket = rate
    }

    if (bucket < 1.0) {
      return true

    } else {
      bucket -= 1.0
      return false
    }
  }

  @Override
  String toString() {
    MoreObjects
        .toStringHelper(this)
        .add('rate', rate)
        .add('perMillis', perMillis)
        .toString()
  }
}
