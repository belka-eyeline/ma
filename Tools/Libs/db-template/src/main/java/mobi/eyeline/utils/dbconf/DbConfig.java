package mobi.eyeline.utils.dbconf;

import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * DB-stored configuration.
 */
public interface DbConfig {

  String get(String key);

  Map<String, String> getString();
  Map<String, Integer> getInteger();
  Map<String, Long> getLong();
  Map<String, Double> getDouble();
  Map<String, Boolean> getBoolean();
  Map<String, Pattern> getPattern();
  Map<String, URL> getUrl();
  Map<String, URI> getUri();
}
