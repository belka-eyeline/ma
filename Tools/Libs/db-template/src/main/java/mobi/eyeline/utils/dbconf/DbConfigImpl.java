package mobi.eyeline.utils.dbconf;

import com.google.common.base.Function;
import mobi.eyeline.utils.db.DBService;
import mobi.eyeline.utils.db.DBService.Stateless;
import mobi.eyeline.utils.dbconf.model.Property;
import org.apache.log4j.Logger;
import org.hibernate.StatelessSession;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang3.StringUtils.trimToNull;
import static org.hibernate.criterion.Restrictions.eq;

public class DbConfigImpl implements DbConfig {

  private final Logger log = Logger.getLogger(DbConfigImpl.class);

  private final DBService db;
  private final DbPropertiesConfig props;

  private final Class<? extends Property> propertyClass;

  private DbConfigChangedEventListener eventListener;

  public DbConfigImpl(DBService db, DbPropertiesConfig props) {
    this(db, Property.class, props);
  }

  public DbConfigImpl(DBService db,
                      Class<? extends Property> propertyClass,
                      DbPropertiesConfig props) {
    this.db = db;
    this.propertyClass = propertyClass;
    this.props = props;

    startRefresh();
  }

  public void setEventListener(DbConfigChangedEventListener eventListener) {
    this.eventListener = eventListener;
  }

  //
  //  Typed views.
  //

  @Override
  public Map<String, Integer> getInteger() {
    return proxy(new Function<String, Integer>() {
      @Override
      public Integer apply(String _) {
        return Integer.valueOf(_);
      }
    });
  }

  @Override
  public Map<String, Long> getLong() {
    return proxy(new Function<String, Long>() {
      @Override
      public Long apply(String _) {
        return Long.valueOf(_);
      }
    });
  }

  @Override
  public Map<String, Double> getDouble() {
    return proxy(new Function<String, Double>() {
      @Override
      public Double apply(String _) {
        return Double.valueOf(_);
      }
    });
  }

  @Override
  public Map<String, String> getString() {
    return proxy(new Function<String, String>() {
      @Override
      public String apply(String _) {
        return _;
      }
    });
  }

  @Override
  public Map<String, Boolean> getBoolean() {
    return proxy(new Function<String, Boolean>() {
      @Override
      public Boolean apply(String _) {
        return Boolean.valueOf(_);
      }
    });
  }

  @Override
  public Map<String, Pattern> getPattern() {
    return proxy(new Function<String, Pattern>() {
      @Override
      public Pattern apply(String _) {
        return Pattern.compile(_);
      }
    });
  }

  @Override
  public Map<String, URL> getUrl() {
    return proxy(new Function<String, URL>() {
      @Override
      public URL apply(String _) {
        try {
          return new URL(_);

        } catch (MalformedURLException e) {
          throw new RuntimeException("Invalid value: [" + _ + "]", e);
        }
      }
    });
  }

  @Override
  public Map<String, URI> getUri() {
    return proxy(new Function<String, URI>() {
      @Override
      public URI apply(String _) {
        try {
          return new URI(_);

        } catch (URISyntaxException e) {
          throw new RuntimeException("Invalid value: [" + _ + "]", e);
        }
      }
    });
  }


  //
  //  Untyped operator accessors.
  //

  @Override
  public String get(String key) {
    return cache != null ? cache.get(key) : loadValue(key);
  }


  //
  //  DB cache.
  //

  protected volatile Map<String, String> cache;
  private Timer timer;

  private void startRefresh() {
    if (props.getDbPropertyRefreshMillis() > 0) {
      reload();

      timer = new Timer("property-refresh", true);
      timer.schedule(new RefreshTask(), props.getDbPropertyRefreshMillis(), props.getDbPropertyRefreshMillis());
    }
  }

  private Map<String, String> loadAll() {
    return db.stateless(new Stateless<Map<String, String>>() {

      @Override
      public Map<String, String> run(StatelessSession s) {
        @SuppressWarnings("unchecked")
        final List<Property> properties =
            (List<Property>) s.createCriteria(propertyClass).list();

        return new LinkedHashMap<String, String>() {{
          for (Property property : properties) {
            put(property.getKey(), property.getValue());
          }
        }};
      }
    });
  }

  private void reload() {
    final Map<String, String> prev = cache;

    cache = loadAll();

    if (!cache.equals(prev)) {
      if (eventListener != null) {
        eventListener.onConfigChanged(new DbConfigChangedEvent());
      }
    }
  }

  private String loadValue(final String key) {
    checkNotNull(key);

    return db.stateless(new Stateless<String>() {
      @Override
      public String run(StatelessSession s) {
        final Property p = (Property) s
            .createCriteria(propertyClass)
            .add(eq("key", key))
            .uniqueResult();

        if (p == null) {
          return null;
        }

        return trimToNull(p.getValue());
      }
    });
  }

  @SuppressWarnings("WeakerAccess")
  public <T> Map<String, T> proxy(Function<String, T> converter) {
    return new PropertyProxy<T>(this, converter);
  }

  //
  //
  //

  private class RefreshTask extends TimerTask {

    @Override
    public void run() {
      try {
        reload();

      } catch (Exception e) {
        log.warn("Property update failed", e);
      }
    }
  }


  //
  //
  //

  private static class PropertyProxy<V> implements Map<String, V> {

    private final DbConfig impl;
    private final Function<String, V> converter;

    PropertyProxy(DbConfig impl,
                  Function<String, V> converter) {
      this.impl = impl;
      this.converter = converter;
    }

    private String rawValue(String key) {
      return impl.get(key);
    }

    @Override
    public V get(Object key) {
      final String _ = rawValue(key.toString());
      return _ == null ? null : converter.apply(_);
    }

    @Override public int size() { throw new UnsupportedOperationException(); }
    @Override public boolean isEmpty() { throw new UnsupportedOperationException(); }
    @Override public boolean containsKey(Object key) { throw new UnsupportedOperationException(); }
    @Override public boolean containsValue(Object value) { throw new UnsupportedOperationException(); }
    @Override public V put(String key, V value) { throw new UnsupportedOperationException(); }
    @Override public V remove(Object key) { throw new UnsupportedOperationException(); }
    @Override public void putAll(Map<? extends String, ? extends V> m) { throw new UnsupportedOperationException(); }
    @Override public void clear() { throw new UnsupportedOperationException(); }
    @Override public Set<String> keySet() { throw new UnsupportedOperationException(); }
    @Override public Collection<V> values() { throw new UnsupportedOperationException(); }
    @Override public Set<Map.Entry<String, V>> entrySet() { throw new UnsupportedOperationException(); }
  }
}
