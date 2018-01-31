package mobi.eyeline.utils.db.test;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.FileSystemResourceAccessor;
import mobi.eyeline.utils.db.test.TestDbConnectionBuilder.DbConnectionDetails;
import mobi.eyeline.utils.db.test.TestDbConnectionBuilder.DbInitializer;
import org.hibernate.jdbc.Work;

import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;

import static mobi.eyeline.utils.db.test.TestDbConnectionBuilder.isInitialized;


public class Initializers {

  public static DbInitializer ifNotInitialized(final DbInitializer delegate) {
    return new DbInitializer() {
      @Override
      public void run(TestDbConnectionBuilder.DbConnectionDetails connection) throws Exception {
        if (!isInitialized(connection)) {
          connection.run(delegate);
        }
      }

      @Override
      public String toString() {
        return "IfNotInitialized[" + delegate.toString() + "]";
      }
    };
  }

  private static void ensureXercesIsUsed() {
    System.setProperty(
        "javax.xml.parsers.DocumentBuilderFactory",
        "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");
    System.setProperty(
        "javax.xml.parsers.SAXParserFactory",
        "com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl");
  }


  //////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Initializes ready to use schema using Liquibase changeset.
   */
  public static class InitializeSchema implements DbInitializer {

    private final URL liquibaseChangeset;

    public InitializeSchema(URL liquibaseChangeset) {
      this.liquibaseChangeset = liquibaseChangeset;
    }

    @Override
    public void run(final DbConnectionDetails _) throws SQLException, LiquibaseException {
      _.doWorkInSchema(new Work() {
        @Override
        public void execute(Connection connection) throws SQLException {
          try {
            liquibaseUpdate(connection);

          } catch (LiquibaseException e) {
            throw new SQLException(e);
          }
        }
      });

    }

    /** Run Liquibase migration utility using a provided changeset. */
    protected void liquibaseUpdate(Connection conn) throws LiquibaseException {
      ensureXercesIsUsed();

      final Database database = DatabaseFactory.getInstance()
          .findCorrectDatabaseImplementation(new JdbcConnection(conn));

      final Liquibase liquibase =
          new Liquibase(liquibaseChangeset.getPath(), new FileSystemResourceAccessor(), database);

      liquibase.update(new Contexts(), new LabelExpression());
    }
  }


  //////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Deletes all data leaving the schema intact.
   */
  abstract static class BaseCleanData implements DbInitializer {

    protected final boolean cacheTableNames;

    /**
     * @param cacheTableNames If set, table names for cleanup will be cached across calls.
     *                        <p>
     *                        Enabled by default and seems to be safe in case no schema changes
     *                        are performed during test execution.
     */
    public BaseCleanData(boolean cacheTableNames) {
      this.cacheTableNames = cacheTableNames;
    }

    public BaseCleanData() {
      this(true);
    }

    @Override
    public void run(final DbConnectionDetails _) throws Exception {
      _.doWorkInSchema(new Work() {
        @Override
        public void execute(Connection connection) throws SQLException {
          truncateAllTables(_, connection, _.getDbName());
        }
      });
    }

    protected abstract void truncateAllTables(DbConnectionDetails _,
                                              Connection conn,
                                              String dbName) throws SQLException;

  }


}
