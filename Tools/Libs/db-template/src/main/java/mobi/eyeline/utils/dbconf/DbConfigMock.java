package mobi.eyeline.utils.dbconf;

import java.util.Collections;
import java.util.Map;

/**
 * Implementation of {@linkplain DbConfig DbConfig} with static value set.
 * Useful for automated tests.
 */
public class DbConfigMock extends DbConfigImpl {

  public DbConfigMock(Map<String, String> values) {
    super(null, DbPropertiesConfig.DEFAULT);
    cache = Collections.unmodifiableMap(values);
  }

}
