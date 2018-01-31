package mobi.eyeline.utils.general.misc

import com.google.common.util.concurrent.Striped
import groovy.transform.CompileStatic

import java.util.concurrent.locks.Lock

/**
 * Utility wrappers for Guava locks and other implementations.
 */
@CompileStatic
class Locks {

  static StripedLock wrap(Striped<Lock> _) { new StripedLock(_) }

  @CompileStatic
  static class StripedLock {

    private final Striped<Lock> delegate

    StripedLock(Striped<Lock> _) { delegate = _ }

    /**
     * Invokes provided closure while holding a lock identified by {@code id}.
     */
    def <T> T on(Object id, Closure<T> action) {
      final lock = get id

      lock.lock()
      try {
        action()

      } finally {
        lock.unlock()
      }
    }

    Lock get(Object id) { delegate.get id }
  }

  static <T> T locked(Lock lock, Closure<T> action) {
    lock.lock()
    try {
      return action.call()

    } finally {
      lock.unlock()
    }
  }
}
