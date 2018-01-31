package mobi.eyeline.utils.dbconf;

/**
 * Configuration for {@linkplain DbConfig} {@linkplain DbConfigImpl implementation}.
 */
public interface DbPropertiesConfig {

  /** No caching. */
  DbPropertiesConfig DEFAULT = new DbPropertiesConfig() {
    @Override
    public long getDbPropertyRefreshMillis() {
      return -1;
    }
  };


  /**
   * Value above zero indicates property refresh interval, otherwise internal caching is disabled
   * and every access results in database operation.
   *
   * @return Refresh interval, millis.
   */
  long getDbPropertyRefreshMillis();

}
