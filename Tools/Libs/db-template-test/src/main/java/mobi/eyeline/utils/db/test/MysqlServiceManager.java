package mobi.eyeline.utils.db.test;

import com.mysql.management.MysqldResource;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Map;

class MysqlServiceManager {

  private static Logger log = Logger.getLogger(TestDBServiceFactory.class);

  /** DB server reference for the current process. */
  private static volatile MysqldResource dbResource;

  private static File dbDir;

  /**
   * In case no managed MySQL instance is running, starts it using the options provided.
   * Otherwise this call is ignored.
   *
   * @return {@code true} iff embedded MySQL instance was not running and has been started.
   */
  public static synchronized boolean ensureStarted(final Map<String, String> opts, long timeoutMillis) {
    if (dbResource == null) {

      if (timeoutMillis <= 0) {
        dbResource = startDatabase(opts);

      } else {
        final Thread initializerThread = new Thread(new Runnable() {
          @Override
          public void run() {
            dbResource = startDatabase(opts);
          }
        }, "db-initializer");

        initializerThread.setDaemon(true);
        initializerThread.start();

        try {
          initializerThread.join(timeoutMillis);

        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new RuntimeException(e);
        }

        if (dbResource == null) {
          initializerThread.interrupt();

          throw new RuntimeException(
              "Database initialization using parameters [" + opts + "] timed out");
        }
      }

      return true;
    }

    return false;
  }

  private static MysqldResource startDatabase(Map<String, String> opts) {
    log.info("Starting MySQL server.");

    dbDir = createTempDirectory();
    final MysqldResource mysqldResource = new MysqldResource(dbDir);

    mysqldResource.start("mysqld", opts);

    if (!mysqldResource.isRunning()) {
      throw new RuntimeException("MySQL did not start.");
    }

    log.info("MySQL is running.");
    return mysqldResource;
  }

  static {
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        log.info("Stopping MySQL server");
        if (dbResource != null) {
          dbResource.shutdown();
          dbResource = null;
        }

        if (dbDir != null) {
          if (!deleteDirectory(dbDir)) {
            throw new RuntimeException(
                "Cannot delete MySQL temp: [" + dbDir.getAbsolutePath() + "]");
          }
          dbDir = null;
        }
      }
    });
  }


  //
  //  File utils.
  //

  private static boolean deleteDirectory(File dir) {
    if (dir.isDirectory()) {
      //noinspection ConstantConditions
      for (File kid : dir.listFiles()) {
        if (!deleteDirectory(kid)) return false;
      }
    }
    return dir.delete();
  }

  private static File createTempDirectory() {
    final File temp;
    try {
      temp = File.createTempFile("temp", Long.toString(System.nanoTime()));

    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    if (!temp.delete()) {
      throw new RuntimeException("Could not delete temp file: " + temp.getAbsolutePath());
    }

    if (!temp.mkdir()) {
      throw new RuntimeException("Could not create temp directory: " + temp.getAbsolutePath());
    }

    return temp;
  }
}
