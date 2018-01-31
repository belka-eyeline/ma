package mobi.eyeline.utils.general.caches

import com.google.common.base.MoreObjects
import com.google.common.base.Preconditions
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import groovy.transform.CompileStatic
import mobi.eyeline.utils.general.types.Duration

@CompileStatic
class LimitedCache<T> extends ReloadableCacheBase<String, T> {
  private Duration lifeTime
  private int maxSize

  @Override
  protected Cache<String, Boolean> buildCache() {
    Preconditions.checkState lifeTime != null

    CacheBuilder
        .newBuilder()
        .expireAfterAccess(lifeTime.value, lifeTime.unit)
        .maximumSize(maxSize)
        .build()
  }

  LimitedCache<T> setProperties(Duration lifeTime, int maxSize) {
    final changed = (this.lifeTime != lifeTime) || (this.maxSize != maxSize)

    this.lifeTime = lifeTime
    this.maxSize = maxSize

    if (changed) {
      reload()
    }

    this
  }

  T getIfPresent(String key) { withCache { it.getIfPresent key } as T }
  void put(String key, T value) { withCache { it.put key, value } as T }


  @Override
  String toString() {
    MoreObjects
        .toStringHelper(this)
        .add('lifeTime', lifeTime)
        .add('maxSize', maxSize)
        .toString()
  }
}