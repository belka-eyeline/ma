package mobi.eyeline.utils.db;

import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;

/**
 * Annotate an entity class to make it readonly:
 * <pre>
 *   &#064;Entity
 *   &#064;EntityListeners({ForceReadonlyEntityListener.class})
 *   class MyEntity { ... }
 * </pre>
 *
 * Note that checks are performed at runtime.
 */
@SuppressWarnings("WeakerAccess")
public class ForceReadonlyEntityListener {

  @PrePersist
  public void onPrePersist(Object _) { fail(_); }

  @PreUpdate
  public void onPreUpdate(Object _) { fail(_); }

  @PreRemove
  public void onPreRemove(Object _) { fail(_); }

  private void fail(Object o) {
    throw new RuntimeException("This entity is readonly: [" + o + "]");
  }
}