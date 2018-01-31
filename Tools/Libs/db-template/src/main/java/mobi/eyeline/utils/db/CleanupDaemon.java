package mobi.eyeline.utils.db;

import org.apache.log4j.Logger;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.util.HashSet;
import java.util.Set;

public class CleanupDaemon {

  private static final Logger logger = Logger.getLogger(CleanupDaemon.class);

  private static final ReferenceQueue<CleanupPhantomRef> UNREACHABLE =
      new ReferenceQueue<CleanupPhantomRef>();

  /** Avoids collecting references before their referents. */
  @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
  private static final Set<CleanupPhantomRef> REFS = new HashSet<CleanupPhantomRef>();

  /**
   * Schedules {@code cleanup} to execute once {@code referent} becomes unreachable and GCed.
   */
  public static void registerCleanup(Object referent, Runnable cleanup) {
    synchronized (REFS) {
      REFS.add(new CleanupPhantomRef(referent, cleanup));
    }
  }

  public static void ensureInitialized() { /* Nothing here */ }

  private CleanupDaemon() { }


  //
  // Schedule a background thread to process any reference once the referent becomes unreachable.
  //
  private static volatile boolean THREAD_STOPPED = false;
  private static final Thread THREAD = new Thread() {
    @Override
    public void run() {
      while (!THREAD_STOPPED) {
        try {
          ((CleanupPhantomRef) UNREACHABLE.remove()).runCleanup();

        } catch (InterruptedException e) {
          return;
        }
      }
    }

    {
      setName("Resource cleanup");
      setDaemon(true);
      setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread t, Throwable e) {
          try {
            logger.error("Cleanup task failed", e);
          } catch (Throwable ignored) {}
        }
      });

      start();
    }
  };


  //
  // Force processing all _already scheduled for cleanup_ references on application shutdown.
  //
  static {
    final Thread hook = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          THREAD_STOPPED = true;
          THREAD.interrupt();

            CleanupPhantomRef ref;
            while (null != (ref = (CleanupPhantomRef) UNREACHABLE.poll())) {
              ref.runCleanup();
            }
        } catch (Throwable e) {
          logger.error(e.getMessage(), e);
        }
      }
    }, "Resource cleanup shutdown hook");

    Runtime.getRuntime().addShutdownHook(hook);
  }

  //
  //
  //

  private static final class CleanupPhantomRef extends PhantomReference {
    private final Runnable cleanup;

    void runCleanup() {
      synchronized (REFS) {
        REFS.remove(this);
        try {
          cleanup.run();

        } catch (Throwable e) {
          logger.error("Cleanup task failed", e);
        }
      }
    }

    CleanupPhantomRef(Object referent, Runnable cleanup) {
      //noinspection unchecked
      super(referent, UNREACHABLE);
      this.cleanup = cleanup;
    }
  }

}
