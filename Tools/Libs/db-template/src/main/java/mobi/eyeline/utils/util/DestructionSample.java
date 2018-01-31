package mobi.eyeline.utils.util;

import mobi.eyeline.utils.db.DBService;
import mobi.eyeline.utils.db.DBServiceImpl;
import org.hibernate.SessionFactory;

import java.util.Properties;

/**
 * Demonstrates deferred resource destruction.
 * Expected output is the following (in the exact same order):
 *
 * <pre>
 *   Deferred resource cleanup scheduled
 *   Press any key to make resource unreachable
 *
 *   Reference is cleared, trying to force GC...
 *   Hooray! Actual resource cleanup invoked.
 *   Exiting.
 * </pre>
 */
public class DestructionSample {

  public static void main(String[] args) throws Exception {

    final DBService db = new DBServiceImpl((Properties) null, null) {

      @Override
      protected SessionFactory buildSessionFactory(Properties hibernateProperties,
                                                   String... configResource) {
        return null;
      }

      @Override
      public void destroy() {
        System.out.println("Hooray! Actual resource cleanup invoked.");
      }
    };

    @SuppressWarnings("unchecked")
    RepositoryResource storage = new RepositoryResource(db, new Object()) {{
            setDeferDestruction(true);
          }};

    storage.destroy();
    System.out.println("Deferred resource cleanup scheduled");

    tryForceGc();

    System.out.println("Press any key to make resource unreachable");

    //noinspection ResultOfMethodCallIgnored
    System.in.read();

    //noinspection UnusedAssignment
    storage = null;
    System.out.println("Reference is cleared, trying to force GC...");

    tryForceGc();

    System.out.println("Exiting.");
  }

  private static void tryForceGc() throws InterruptedException {
    Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
    for (int n = 0; n < 100; n++) {
      Thread.sleep(10);
      System.gc();
    }
  }
}
