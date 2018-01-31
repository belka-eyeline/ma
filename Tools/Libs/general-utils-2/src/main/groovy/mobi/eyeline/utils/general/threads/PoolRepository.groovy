package mobi.eyeline.utils.general.threads

import groovy.transform.CompileStatic
import groovy.util.logging.Log4j

import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

import static mobi.eyeline.utils.general.threads.FactoryWithCounter.factoryWithCounter

@Log4j
@CompileStatic
class PoolRepository {

  protected final List<ExecutorService> pools = []

  protected ScheduledExecutorService newSingleThreadScheduledExecutor(
      String name,
      boolean daemon = true) {

    Executors.newSingleThreadScheduledExecutor(factoryWithCounter(name, daemon)).with {
      pools << it
      it
    }
  }

  protected ExecutorService newFixedThreadPool(int nThreads, String name) {
    Executors.newFixedThreadPool(nThreads, factoryWithCounter(name)).with {
      pools << it
      it
    }
  }

  void stop() {
    log.info "Stopping thread pools"
    pools.unique().each { stop0 it }
  }

  protected <T extends ExecutorService> T register(T pool) {
    pools << pool
    pool
  }

  @SuppressWarnings('GrMethodMayBeStatic')
  private void stop0(ExecutorService executor) {
    if (!executor.isShutdown()) {
      executor.shutdown()
    }

    if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
      log.error 'Failed to shutdown in a timely manner'
    }
  }


  //
  //
  //

  @CompileStatic
  static class WorkerPool extends ThreadPoolExecutor {

    WorkerPool(String name,
               int fixedPoolSize = Runtime.runtime.availableProcessors(),
               int queueSize = 32) {
      //noinspection UnnecessaryQualifiedReference
      super(
          fixedPoolSize,
          fixedPoolSize,
          30L,
          TimeUnit.SECONDS,
          new ArrayBlockingQueue<Runnable>(queueSize),
          factoryWithCounter(name, true),
          new ThreadPoolExecutor.CallerRunsPolicy()
      )
    }
  }
}
