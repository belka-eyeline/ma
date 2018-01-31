package mobi.eyeline.utils.pdfgenerator.util

import groovy.transform.CompileStatic

import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue

/**
 * Fixed resource pool implementation.
 */
@CompileStatic
abstract class ResourcePool<T> {
  
  private final BlockingQueue<T> resources

  ResourcePool(final int nResources) throws Exception {
    this.resources =
        new ArrayBlockingQueue<T>(nResources, false, (0..<nResources).collect { init() })
  }

  /**
   * Initializes guarded resource instance.
   */
  protected abstract T init() throws Exception

  /**
   * Executes an action using one of the guarded resources, waits for availability if needed.
   *
   * @throws InterruptedException If interrupted while waiting for resource availability
   * @throws Exception Exceptions raised in the supplied action are re-thrown.
   */
  final <V> V call(ResourceCallable<V, T> action) {
    final T instance = resources.take()

    try {
      return action.call(instance)

    } finally {
      resources.put instance
    }
  }

  final void run(final ResourceVoidCallable<T> action) throws Exception {
    call { T resource ->
      action.call(resource)
    }
  }


  static interface ResourceCallable<V, T> {
    V call(T resource) throws Exception
  }

  static interface ResourceVoidCallable<T> {
    void call(T resource) throws Exception
  }
}
