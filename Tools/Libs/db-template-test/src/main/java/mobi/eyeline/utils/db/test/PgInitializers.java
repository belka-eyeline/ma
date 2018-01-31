package mobi.eyeline.utils.db.test;

import mobi.eyeline.utils.db.test.TestDbConnectionBuilder.DbConnectionDetails;
import org.hibernate.jdbc.Work;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static mobi.eyeline.utils.db.test.TestDbConnectionBuilder.DbInitializer;

/**
 * PostgreSQL-specific stuff.
 */
@SuppressWarnings({"SqlNoDataSourceInspection", "SqlResolve"})
public class PgInitializers extends Initializers {

  //////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Creates database if not exists.
   */
  public static class CreateDatabaseIfNotExists implements DbInitializer {

    @Override
    public void run(final DbConnectionDetails _) throws SQLException {
      _.doWork(new Work() {

        @Override
        public void execute(Connection connection) throws SQLException {
          if (dbExists(connection, _.getDbName())) {
            // Okay, DB exists.

          } else {
            final Statement statement = connection.createStatement();
            try {
              statement.execute("CREATE DATABASE " + _.getDbName() + "");

            } finally {
              statement.close();
            }
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

          if (dbExists(connection, _.getDbName())) {
            final Statement statement = connection.createStatement();
            try {
              statement.execute("DROP DATABASE " + _.getDbName() + "");

            } finally {
              statement.close();
            }
          }

          final Statement statement = connection.createStatement();
          try {
            statement.execute("CREATE DATABASE " + _.getDbName() + "");

          } finally {
            statement.close();
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
    public CleanData() {}

    @Override
    protected void truncateAllTables(DbConnectionDetails _,
                                     Connection conn,
                                     String dbName) throws SQLException {

      final Statement s = conn.createStatement();
      try {
        s.execute(getTruncateStatement(conn));

      } finally {
        s.close();
      }
    }

  }


  //////////////////////////////////////////////////////////////////////////////////////////////////

  private static boolean dbExists(Connection connection, String dbName) throws SQLException {
    final Statement s = connection.createStatement();
    try {
      final ResultSet rc =
          s.executeQuery("SELECT 1 FROM pg_database WHERE datname = '" + dbName + "'");
      return rc.next();

    } finally {
      s.close();
    }
  }

  static String getTruncateStatement(Connection connection) throws SQLException {
    final Statement s = connection.createStatement();

    try {
      final ResultSet rc = s.executeQuery("" +
          "SELECT " +
          "  'TRUNCATE TABLE '" +
          "    || string_agg(quote_ident(schemaname) || '.' || quote_ident(tablename), ', ')" +
          "    || ' RESTART IDENTITY CASCADE'" +
          "FROM pg_tables " +
          "WHERE" +
          "  schemaname = 'public' AND" +
          "  tablename NOT IN ('databasechangelog', 'databasechangeloglock');");

      try {
        rc.next();
        return rc.getString(1);

      } finally {
        rc.close();
      }

    } finally {
      s.close();
    }
  }
}
