package mobi.eyeline.utils.db.test;

import mobi.eyeline.utils.db.DBService;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import java.util.Collection;

public abstract class DBTest {

  protected DBService db;

  @Before
  public abstract void dbInit() throws Exception;

  @After
  public final void dbDestroy() {
    db.destroy();
  }


  //
  //
  //

  protected void assertValid(Object o) {
    Assert.assertTrue(db.isValidObject(o));
  }

  protected void assertInvalid(Object o) {
    Assert.assertFalse(db.isValidObject(o));
  }

  protected void assertCollectionsEqualsIgnoreOrder(Collection c1, Collection c2) {
    Assert.assertEquals(c1.size(), c2.size());
    for (Object o1 : c1) {
      Assert.assertTrue(c2.contains(o1));
    }
  }

  protected int countEntities(final Class<?> type) {
    return db.query(new DBService.Tx<Integer>() {
      @Override
      public Integer tx(Session s) {
        final Number count = (Number) s
            .createCriteria(type)
            .setProjection(Projections.rowCount())
            .uniqueResult();
        return count.intValue();
      }
    });
  }
}
