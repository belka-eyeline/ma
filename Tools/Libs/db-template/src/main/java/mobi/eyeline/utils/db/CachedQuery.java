package mobi.eyeline.utils.db;

import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;

/**
 * Cached queries wrapper.
 */
public class CachedQuery {

  private final DetachedCriteria wrapped;
  private final String cacheRegion;

  public CachedQuery(DetachedCriteria baseCriteria, String cacheRegion) {
    this.wrapped = baseCriteria;
    this.cacheRegion = cacheRegion;
  }

  public Criteria getExecutableCriteria(Session session) {
    Criteria result = wrapped.getExecutableCriteria(session);
    if (cacheRegion != null) {
      result.setCacheable(true);
      result.setCacheRegion(cacheRegion);
      result.setCacheMode(CacheMode.NORMAL);
    }
    return result;
  }

}
