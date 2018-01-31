package mobi.eyeline.utils.db;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.CriteriaQuery;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.LikeExpression;
import org.hibernate.criterion.MatchMode;
import org.hibernate.dialect.HSQLDialect;
import org.hibernate.engine.spi.SessionFactoryImplementor;


/**
 * {@linkplain org.hibernate.criterion.Criterion Criterion} builders which provide escaping of
 * DB special symbols like {@literal \}, {@literal _} and {@literal %}.
 */
public class EscapedRestrictions {

  public static final String LIKE_ESCAPE_CHARACTER = "\\";

  @SuppressWarnings("SpellCheckingInspection")
  public static Criterion ilike(String propertyName, String value) {
    return new EscapedILikeExpression(propertyName, value);
  }

  @SuppressWarnings("SpellCheckingInspection")
  public static Criterion ilike(String propertyName, String value, MatchMode matchMode) {
    return new EscapedILikeExpression(propertyName, value, matchMode);
  }

  static String getEscapeExpression(SessionFactoryImplementor factory) {
    if (factory.getDialect() instanceof HSQLDialect) {
      return "ESCAPE '\\'";
    } else {
      return "ESCAPE '\\\\'";
    }
  }

  private static class EscapedILikeExpression extends LikeExpression {

    public EscapedILikeExpression(String propertyName, String value) {
      super(propertyName, replaceAll(value), null, true);
    }

    public EscapedILikeExpression(String propertyName, String value, MatchMode matchMode) {
      super(propertyName, replaceAll(value), matchMode, null, true);
    }

    @Override
    public String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery)
        throws HibernateException {
      final String escapeExpression = getEscapeExpression(criteriaQuery.getFactory());
      return super.toSqlString(criteria, criteriaQuery) + escapeExpression;
    }

    private static String replaceAll(String value) {
      return value
          .replace("\\", LIKE_ESCAPE_CHARACTER + "\\")
          .replace("_", LIKE_ESCAPE_CHARACTER + "_")
          .replace("%", LIKE_ESCAPE_CHARACTER + "%");
    }
  }
}
