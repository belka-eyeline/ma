package mobi.eyeline.utils.general.caches

import com.google.common.base.MoreObjects
import com.google.common.base.Preconditions
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import groovy.transform.CompileStatic
import mobi.eyeline.utils.general.types.Duration

import java.util.concurrent.Callable

/**
 * Cache wrapper with runtime-configurable lifetime.
 */
@CompileStatic
class ExpiringCache<T> extends ReloadableCacheBase<String, T> {

  private Duration lifeTime

  @Override
  protected Cache<String, T> buildCache() {
    Preconditions.checkState lifeTime != null

    CacheBuilder
        .newBuilder()
        .expireAfterWrite(lifeTime.value, lifeTime.unit)
        .build()
  }

  Duration getLifeTime() {
    lifeTime
  }

  ExpiringCache<T> setLifeTime(Duration lifeTime) {
    final changed = (this.lifeTime != lifeTime)

    this.lifeTime = lifeTime

    if (changed) {
      reload()
    }

    this
  }

  T get(String key, Callable<T> loader = null) {
    loader ? withCache { it.get(key, loader) } as T : withCache { it.getIfPresent(key) } as T
  }

  void remove(String key) {
    withCache { impl -> impl.invalidate key }
  }

  @Override
  String toString() {
    MoreObjects
        .toStringHelper(this)
        .add('lifeTime', lifeTime)
        .toString()
  }
}
