package mobi.eyeline.utils.general.misc;

public class Reiterator {

  private final int errorCount;

  public Reiterator(int errorCount) {
    assert errorCount >= 0;
    this.errorCount = errorCount;
  }

  public final <R> R call(Action<R> action) throws Exception {
    int nAttempt = 0;

    while (true) {
      try {
        onBeforeCall(nAttempt);
        return action.call();

      } catch (Exception e) {
        onError(e);

        // Silently ignore.
        if (shouldIgnore(e) && (nAttempt < errorCount))   onIgnored(e, nAttempt++);
        else                                              throw e;
      }
    }
  }

  public final void vcall(final VoidAction action) throws Exception {
    call(new Action<Void>() {
      @Override
      public Void call() throws Exception {
        action.call();
        return null;
      }
    });
  }

  protected boolean shouldIgnore(Exception e) {
    // Ignore all types of exceptions by default.
    return true;
  }

  /**
   * Called before any action call.
   *
   * @param nAttempt  Number of the current attempt, starting with 0.
   */
  protected void onBeforeCall(int nAttempt) {
    // Nothing here.
  }

  /**
   * Called on any action invocation error occurred, no matter ignored or not.
   */
  protected void onError(Exception e) {
    // Nothing here.
  }

  /**
   * Called on an error which would be {@link #shouldIgnore(Exception) ignored}.
   *
   * @param e         An error which occurred during action execution.
   * @param nAttempt  Number of the current attempt, starting with {@code 0}.
   */
  protected void onIgnored(Exception e, int nAttempt) {
    // Nothing here.
  }

  public interface Action<R> {
    R call() throws Exception;
  }

  public interface VoidAction {
    void call() throws Exception;
  }

}