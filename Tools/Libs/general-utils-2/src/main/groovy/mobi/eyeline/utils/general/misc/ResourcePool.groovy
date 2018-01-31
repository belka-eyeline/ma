package mobi.eyeline.utils.general.misc

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.TypeCheckingMode

import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue

/**
 * Fixed resource pool implementation.
 */
@CompileStatic
abstract class ResourcePool<T> {

  private final BlockingQueue<T> resources

  ResourcePool(int nResources) throws Exception {
    resources = new ArrayBlockingQueue<T>(nResources, false, (0..<nResources).collect { init() })
  }

  @PackageScope
  ResourcePool(int nResources, Closure<T> initializer) throws Exception {
    resources = new ArrayBlockingQueue<T>(
        nResources, false, (0..<nResources).collect { initializer.call() }
    )
  }

  /**
   * Initializes guarded resource instance.
   */
  protected abstract T init() throws Exception

  /**
   * Executes an action using one of the guarded resources, waits for availability if needed.
   *
   * @throws java.lang.InterruptedException If interrupted while waiting for resource availability
   * @throws java.lang.Exception Exceptions raised in the supplied action are re-thrown.
   */
  final <V> V call(ResourceCallable<V, T> action) throws Exception {
    final T instance = resources.take()

    try {
      return action.call(instance)

    } finally {
      resources.put(instance)
    }
  }

  final void run(final ResourceVoidCallable<T> action) throws Exception {
    call(new ResourceCallable<Void, T>() {
      @Override
      public Void call(T resource) throws Exception {
        action.call(resource)
        return null
      }
    })
  }


  @CompileStatic(TypeCheckingMode.SKIP)
  static <T> ResourcePool<T> fixedPool(int nResources, Closure<T> resourceInit) {
    new ResourcePool<T>(nResources, resourceInit) { @Override protected T init() { null } }
  }


  @CompileStatic
  static interface ResourceCallable<V, T> {
    V call(T resource) throws Exception
  }

  @CompileStatic
  static interface ResourceVoidCallable<T> {
    void call(T resource) throws Exception
  }
}
