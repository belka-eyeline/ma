package mobi.eyeline.utils.general.config;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Replacement for NIO file watcher compatible with 1.6.
 */
public class WatcherThread16 extends Thread {

  private final Logger logger = Logger.getLogger(WatcherThread16.class);

  private final FileChangeListener listener;
  private final File file;

  private final FileChangeTracker fileChangeTracker;

  private long refreshPeriod  = TimeUnit.SECONDS.toMillis(15);
  private long cooldownPeriod = TimeUnit.SECONDS.toMillis(30);


  public WatcherThread16(File file,
                         FileChangeListener listener,
                         FileChangeTracker fileChangeTracker) throws IOException {

    super("cfg-watcher");
    setDaemon(true);

    this.file = file;
    this.listener = listener;
    this.fileChangeTracker = fileChangeTracker;
  }

  public WatcherThread16(File file,
                         FileChangeListener listener) throws IOException {
    this(file, listener, new HashChangeTracker(file));
  }

  public void setRefreshPeriod(long refreshPeriod) {
    this.refreshPeriod = refreshPeriod;
  }

  public void setCooldownPeriod(long cooldownPeriod) {
    this.cooldownPeriod = cooldownPeriod;
  }

  @Override
  public void run() {
    try {
      while (!isInterrupted()) {
        try {
          run0();
          Thread.sleep(refreshPeriod);

        } catch (InterruptedException e) {
          logger.info("Interrupted");
          Thread.interrupted();
          return;

        } catch (Exception e) {
          logger.error("Watcher loop failed", e);
          Thread.sleep(cooldownPeriod);
        }
      }

    } catch (InterruptedException e) {
      logger.info("Interrupted");
      Thread.interrupted();
    }
  }

  private void run0() throws IOException, InterruptedException {
    if (fileChangeTracker.isChanged()) {
      listener.onFileChanged(file);
      fileChangeTracker.reset();
    }
  }


  //
  //
  //

  public interface FileChangeListener {
    void onFileChanged(File path);
  }

  public interface FileChangeTracker {
    File getFile();
    boolean isChanged() throws IOException;
    void reset() throws IOException;
  }


  //
  //
  //

  private static class HashChangeTracker implements FileChangeTracker {

    private final File file;
    private byte[] hash;

    HashChangeTracker(File file) throws IOException {
      this.file = file;
      this.hash = getHash();
    }

    @Override
    public boolean isChanged() throws IOException {
      final byte[] newHash = getHash();
      return !Arrays.equals(hash, newHash);
    }

    @Override
    public void reset() throws IOException {
      hash = getHash();
    }

    @Override
    public File getFile() {
      return file;
    }

    private byte[] getHash() throws IOException {
      return getMessageDigest().digest(FileUtils.readFileToByteArray(file));
    }

    private static MessageDigest getMessageDigest() {
      try {
        return MessageDigest.getInstance("MD5");

      } catch (NoSuchAlgorithmException e) {
        throw new RuntimeException(e);
      }
    }
  }
}