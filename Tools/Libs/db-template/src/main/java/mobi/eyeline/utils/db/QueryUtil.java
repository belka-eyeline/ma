package mobi.eyeline.utils.db;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import java.util.Collection;

public class QueryUtil {

  public static Criterion or(Collection<? extends Criterion> expressions) {
    return Restrictions.or(expressions.toArray(new Criterion[expressions.size()]));
  }

  public static Criterion and(Collection<? extends Criterion> expressions) {
    return Restrictions.and(expressions.toArray(new Criterion[expressions.size()]));
  }
}
