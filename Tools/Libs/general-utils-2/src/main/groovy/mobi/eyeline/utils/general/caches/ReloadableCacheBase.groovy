package mobi.eyeline.utils.general.caches

import com.google.common.cache.Cache
import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType

import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock

import static mobi.eyeline.utils.general.misc.Locks.locked

@CompileStatic
abstract class ReloadableCacheBase<K, V> {

  private final ReadWriteLock lock = new ReentrantReadWriteLock()
  private Cache<K, V> impl

  protected abstract Cache<K, V> buildCache()

  protected void reload() {
    locked lock.writeLock(), { reload0() }
  }

  private void reload0() {
    final prevState = impl?.asMap()

    impl = buildCache()

    if (prevState) {
      impl.putAll prevState
    }
  }

  protected final <T> T withCache(
          @ClosureParams(value = SimpleType, options = 'com.google.common.cache.Cache')
          Closure<T> action) {

    locked lock.readLock(), { action impl }
  }


}
