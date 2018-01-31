package mobi.eyeline.utils.db.test;

import mobi.eyeline.utils.db.test.TestDbConnectionBuilder.DbConnectionDetails;
import mobi.eyeline.utils.db.test.TestDbConnectionBuilder.DbInitializer;
import org.hibernate.jdbc.Work;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;


/**
 * MySQL-specific stuff.
 */
@SuppressWarnings({"SqlNoDataSourceInspection", "SqlResolve"})
public class MySqlInitializers extends Initializers {

  /**
   * Creates database if not exists.
   */
  @SuppressWarnings("SqlNoDataSourceInspection")
  public static class CreateDatabaseIfNotExists implements DbInitializer {

    @Override
    public void run(final DbConnectionDetails _) throws SQLException {
      _.doWork(new Work() {
        @Override
        public void execute(Connection connection) throws SQLException {
          final Statement s = connection.createStatement();

          try {
            s.execute("CREATE DATABASE IF NOT EXISTS " + _.getDbName());

          } finally {
            s.close();
          }
        }
      });


    }

  }


  //////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Drops and creates database.
   */
  public static class RecreateDb implements DbInitializer {

    @Override
    public void run(final DbConnectionDetails _) throws SQLException {
      _.doWork(new Work() {
        @Override
        public void execute(Connection connection) throws SQLException {
          final Statement s = connection.createStatement();

          try {
            s.execute("DROP DATABASE IF EXISTS " + _.getDbName());
            s.execute("CREATE DATABASE IF NOT EXISTS " + _.getDbName());

          } finally {
            s.close();
          }
        }
      });
    }

  }


  //////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Deletes all data leaving the schema intact.
   */
  public static class CleanData extends BaseCleanData {
    private static final Map<String, String> truncateStatementsCache =
        new HashMap<String, String>();

    public CleanData(boolean cacheTableNames) { super(cacheTableNames); }
    public CleanData() {}

    @Override
    protected void truncateAllTables(DbConnectionDetails _,
                                     Connection conn,
                                     String dbName) throws SQLException {

      final String truncateStatement;
      if (cacheTableNames) {
        final String key = _.getDbConnectionUrl();
        if (!truncateStatementsCache.containsKey(key)) {
          truncateStatementsCache.put(key, getTruncateStatement(_.getDbName(), conn));
        }
        truncateStatement = truncateStatementsCache.get(key);

      } else {
        truncateStatement = getTruncateStatement(_.getDbName(), conn);
      }

      final Statement s = conn.createStatement();
      try {
        s.execute(truncateStatement);

      } finally {
        s.close();
      }
    }

  }


  //////////////////////////////////////////////////////////////////////////////////////////////////

  static String getTruncateStatement(String dbName,
                                     Connection connection) throws SQLException {
    final Statement s = connection.createStatement();

    try {
      s.execute("SET SESSION group_concat_max_len = 4096;");
      final ResultSet rc = s.executeQuery("" +
          "SELECT " +
          "  GROUP_CONCAT(CONCAT('TRUNCATE TABLE ', table_name) SEPARATOR ';') " +
          "FROM INFORMATION_SCHEMA.TABLES " +
          "WHERE" +
          " table_schema IN ('" + dbName + "') AND" +
          " table_type <> 'VIEW' AND" +
          " table_name NOT IN ('DATABASECHANGELOG', 'DATABASECHANGELOGLOCK');");

      try {
        rc.first();
        final String truncateStatement = rc.getString(1);
        return "SET foreign_key_checks = 0;" + truncateStatement + ";SET foreign_key_checks = 1;";

      } finally {
        rc.close();
      }

    } finally {
      s.close();
    }
  }

}
