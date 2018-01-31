package mobi.eyeline.utils.general.threads

import groovy.transform.CompileStatic

import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

@CompileStatic
class FactoryWithCounter implements ThreadFactory {

  private final AtomicInteger counter = new AtomicInteger()

  private String prefix
  private boolean daemon

  @Override
  Thread newThread(Runnable r) {
    final t = new Thread(r, "${prefix}-${counter.getAndIncrement()}")
    t.daemon = daemon
    t
  }

  static ThreadFactory factoryWithCounter(String prefix, boolean daemon = false) {
    new FactoryWithCounter(prefix: prefix, daemon: daemon)
  }
}