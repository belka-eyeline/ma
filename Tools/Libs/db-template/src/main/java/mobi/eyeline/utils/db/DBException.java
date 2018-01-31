package mobi.eyeline.utils.db;

@SuppressWarnings("WeakerAccess")
public class DBException extends RuntimeException {

  public DBException(String message) {
    super(message);
  }

  public DBException(Throwable cause) {
    super(cause);
  }

}
